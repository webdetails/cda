package pt.webdetails.cda.exporter;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;

import plugins.org.pentaho.di.robochef.kettle.DynamicTransConfig;
import plugins.org.pentaho.di.robochef.kettle.DynamicTransMetaConfig;
import plugins.org.pentaho.di.robochef.kettle.DynamicTransformation;
import plugins.org.pentaho.di.robochef.kettle.RowProductionManager;
import plugins.org.pentaho.di.robochef.kettle.TableModelInput;
import pt.webdetails.cda.CdaBoot;
import pt.webdetails.cda.dataaccess.kettle.DataAccessKettleAdapter;
import pt.webdetails.cda.dataaccess.kettle.KettleAdapterException;
import pt.webdetails.cda.utils.kettle.RowCountListener;

/**
 * Direct exporter from data access to stream using Kettle
 * 
 * @author Michael Spector
 */
public class DefaultStreamExporter implements RowProductionManager, StreamExporter {

	private static final Log logger = LogFactory
			.getLog(DefaultStreamExporter.class);
	private static long DEFAULT_ROW_PRODUCTION_TIMEOUT = 120;
	private static TimeUnit DEFAULT_ROW_PRODUCTION_TIMEOUT_UNIT = TimeUnit.SECONDS;

	private DataAccessKettleAdapter dataAccess;
	private AbstractKettleExporter exporter;
	private ExecutorService executorService = Executors.newCachedThreadPool();
	private Collection<Callable<Boolean>> inputCallables = new ArrayList<Callable<Boolean>>();

	public DefaultStreamExporter(AbstractKettleExporter exporter,
			DataAccessKettleAdapter dataAccess) {
		this.exporter = exporter;
		this.dataAccess = dataAccess;
	}

	public void export(OutputStream out) throws ExporterException {
		inputCallables.clear();

		try {
			DynamicTransConfig transConfig = new DynamicTransConfig();

			String dataAccessStep = dataAccess
					.getKettleStepDefinition("DataAccess");

			String[] parameterNames = dataAccess.getParameterNames();
			DataRow parameters = dataAccess.getParameters();
			if (parameterNames.length > 0) {
				transConfig.addConfigEntry(DynamicTransConfig.EntryType.STEP,
						"Input",
						"<step><name>Input</name><type>Injector</type></step>");
				dataAccessStep = dataAccessStep.replaceFirst("<lookup/>",
						"<lookup>Input</lookup>");
			}

			transConfig.addConfigEntry(DynamicTransConfig.EntryType.STEP,
					"DataAccess", dataAccessStep);
			transConfig.addConfigEntry(DynamicTransConfig.EntryType.STEP,
					"Export", exporter.getExportStepDefinition("Export"));

			if (parameterNames.length > 0) {
				transConfig.addConfigEntry(DynamicTransConfig.EntryType.HOP,
						"Input", "DataAccess");
			}
			transConfig.addConfigEntry(DynamicTransConfig.EntryType.HOP,
					"DataAccess", "Export");

			if (parameterNames.length > 0) {
				List<String> columnNames = new LinkedList<String>();
				List<Class<?>> columnClasses = new LinkedList<Class<?>>();
				List<Object> values = new LinkedList<Object>();
				for (String parameterName : parameterNames) {
					Object value = parameters.get(parameterName);
					if (value instanceof Object[]) {
						Object[] array = (Object[]) value;
						for (int c = 0; c < array.length; ++c) {
							columnNames.add(parameterName + "_" + c);
							columnClasses.add(array[c].getClass());
							values.add(array[c]);
						}
					} else {
						columnNames.add(parameterName);
						columnClasses.add(value == null ? Object.class : value
								.getClass());
						values.add(value);
					}
				}

				TypedTableModel model = new TypedTableModel(
						columnNames.toArray(new String[columnNames.size()]),
						columnClasses.toArray(new Class[columnClasses.size()]));
				model.addRow(values.toArray());

				TableModelInput input = new TableModelInput();
				transConfig.addInput("Input", input);
				inputCallables.add(input.getCallableRowProducer(model, true));
			}

			RowCountListener countListener = new RowCountListener();
			transConfig.addOutput("Export", countListener);

			ExtendedDynamicTransMetaConfig transMetaConfig = new ExtendedDynamicTransMetaConfig(
					DynamicTransMetaConfig.Type.EMPTY, "Streaming Exporter",
					null, null, dataAccess.getDatabases());
			DynamicTransformation trans = new DynamicTransformation(
					transConfig, transMetaConfig);
			trans.executeCheckedSuccess(null, null, this);
			logger.info(trans.getReadWriteThroughput());

			// Transformation executed ok, let's return the file
			exporter.copyFileToOutputStream(out);

			logger.debug(countListener.getRowsWritten() + " rows written.");

		} catch (KettleAdapterException e) {
			throw new ExporterException(
					"Data access to Kettle adapter exception during "
							+ exporter.getType() + " query ", e);
		} catch (KettleException e) {
			throw new ExporterException("Kettle exception during "
					+ exporter.getType() + " query ", e);
		} catch (Exception e) {
			throw new ExporterException("Unknown exception during "
					+ exporter.getType() + " query ", e);
		}
	}

	public static class ExtendedDynamicTransMetaConfig extends
			DynamicTransMetaConfig {

		private DatabaseMeta[] databases;

		public ExtendedDynamicTransMetaConfig(Type type, String name,
				String configDataSource, RepositoryConfig repoConfig,
				DatabaseMeta[] databases) throws KettleException {
			super(type, name, configDataSource, repoConfig);
			this.databases = databases;
		}

		protected TransMeta getTransMeta(VariableSpace variableSpace)
				throws KettleException {
			TransMeta transMeta = super.getTransMeta(variableSpace);
			if (databases != null) {
				for (DatabaseMeta database : databases) {
					transMeta.addOrReplaceDatabase(database);
				}
			}
			return transMeta;
		}
	}

	public void startRowProduction() {
		String timeoutStr = CdaBoot
				.getInstance()
				.getGlobalConfig()
				.getConfigProperty(
						"pt.webdetails.cda.DefaultRowProductionTimeout");
		long timeout = StringUtil.isEmpty(timeoutStr) ? DEFAULT_ROW_PRODUCTION_TIMEOUT
				: Long.parseLong(timeoutStr);
		String unitStr = CdaBoot
				.getInstance()
				.getGlobalConfig()
				.getConfigProperty(
						"pt.webdetails.cda.DefaultRowProductionTimeoutTimeUnit");
		TimeUnit unit = StringUtil.isEmpty(unitStr) ? DEFAULT_ROW_PRODUCTION_TIMEOUT_UNIT
				: TimeUnit.valueOf(unitStr);
		startRowProduction(timeout, unit);
	}

	public void startRowProduction(long timeout, TimeUnit unit) {
		try {
			List<Future<Boolean>> results = executorService.invokeAll(
					inputCallables, timeout, unit);
			for (Future<Boolean> result : results) {
				result.get();
			}
		} catch (InterruptedException e) {
			logger.error(e);
		} catch (ExecutionException e) {
			logger.error(e);
		}
	}

	public String getMimeType() {
		return exporter.getMimeType();
	}

	public String getAttachmentName() {
		return exporter.getAttachmentName();
	}
}
