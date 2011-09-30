package pt.webdetails.cda.cache.monitor;

import javax.swing.table.TableModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.webdetails.cda.cache.TableCacheKey;
import pt.webdetails.cda.dataaccess.AbstractDataAccess;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.monitor.LocalMapStats;

public class HazelcastCacheMonitor 
{
  
  public static JSONObject getClusterInfo() throws JSONException{
    ClusterInfo cluster = new ClusterInfo( Hazelcast.getCluster());
    return cluster.toJson();
  }
  
  public static JSONObject getMapInfo() throws JSONException {
    MapInfo mapInfo = new MapInfo( AbstractDataAccess.getHazelCache().getMap() );
    return mapInfo.toJson();
  }
  
  public static class ClusterInfo 
  {
    
    public ClusterInfo(Cluster cluster){
      this.cluster = cluster;
    }
    
    Cluster cluster;
    
    JSONObject toJson() throws JSONException
    {
      JSONObject clusterInfo = new JSONObject();
      
      MemberInfo localMember = new MemberInfo( cluster.getLocalMember());
      clusterInfo.put("localMember", localMember.toJson());
      
      JSONArray extMembers = new JSONArray();
      for(Member member : cluster.getMembers())
      {
        if(!member.localMember())
        {
          extMembers.put((new MemberInfo(member)).toJson());
        }
      }
      clusterInfo.put("otherMembers", extMembers);
      
      return clusterInfo;
    }
  }
  
  public static class MemberInfo 
  {
    Member member;
    
    public MemberInfo(Member member){
      this.member = member;
    }
    
    JSONObject toJson() throws JSONException
    {
      JSONObject memberInfo = new JSONObject();
      
      memberInfo.put("address", member.getInetSocketAddress().toString());
      memberInfo.put("isSuperClient", member.isSuperClient());
      
      return memberInfo;
      
    }
  }
  
  
  public static class MapInfo
  {
    LocalMapStats mapStats;
    
    public MapInfo(IMap<TableCacheKey, TableModel> map) {
      mapStats = map.getLocalMapStats();
    }
    
    JSONObject toJson() throws JSONException
    {
      JSONObject mapInfo = new JSONObject();
      
      long ownedCount = mapStats.getOwnedEntryCount();
      long backupCount = mapStats.getBackupEntryCount();
      mapInfo.put("entryCount", ownedCount + backupCount);
      mapInfo.put("ownedCount", ownedCount);
      mapInfo.put("backupCount", backupCount);
      
      
      long mem = mapStats.getBackupEntryMemoryCost() + mapStats.getOwnedEntryMemoryCost();
      mapInfo.put("mapMemSize", mem);
      
      mapInfo.put("hits", mapStats.getHits());
      
      return mapInfo;
    }
  }
    
}
