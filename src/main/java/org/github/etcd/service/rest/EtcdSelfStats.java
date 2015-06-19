package org.github.etcd.service.rest;

public class EtcdSelfStats {

    private String id;
    private String name;
    private LeaderInfo leaderInfo;

    private Long recvAppendRequestCnt;
    private Double recvBandwidthRate;
    private Double recvPkgRate;

    private Long sendAppendRequestCnt;
    private Double sendBandwidthRate;
    private Double sendPkgRate;

    private String startTime;
    private String state;

    @Override
    public String toString() {
        return "EtcdSelfStats [id=" + id + ", name=" + name + ", leaderInfo="
                + leaderInfo + ", state=" + state + "]";
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public LeaderInfo getLeaderInfo() {
        return leaderInfo;
    }
    public void setLeaderInfo(LeaderInfo leaderInfo) {
        this.leaderInfo = leaderInfo;
    }
    public Long getRecvAppendRequestCnt() {
        return recvAppendRequestCnt;
    }
    public void setRecvAppendRequestCnt(Long recvAppendRequestCnt) {
        this.recvAppendRequestCnt = recvAppendRequestCnt;
    }
    public Double getRecvPkgRate() {
        return recvPkgRate;
    }
    public void setRecvPkgRate(Double recvPkgRate) {
        this.recvPkgRate = recvPkgRate;
    }
    public Double getRecvBandwidthRate() {
        return recvBandwidthRate;
    }
    public void setRecvBandwidthRate(Double recvBandwidthRate) {
        this.recvBandwidthRate = recvBandwidthRate;
    }
    public Long getSendAppendRequestCnt() {
        return sendAppendRequestCnt;
    }
    public void setSendAppendRequestCnt(Long sendAppendRequestCnt) {
        this.sendAppendRequestCnt = sendAppendRequestCnt;
    }
    public Double getSendBandwidthRate() {
        return sendBandwidthRate;
    }
    public void setSendBandwidthRate(Double sendBandwidthRate) {
        this.sendBandwidthRate = sendBandwidthRate;
    }
    public Double getSendPkgRate() {
        return sendPkgRate;
    }
    public void setSendPkgRate(Double sendPkgRate) {
        this.sendPkgRate = sendPkgRate;
    }

    public static class LeaderInfo {
        private String leader;
        private String uptime;
        private String startTime;
        public String getLeader() {
            return leader;
        }
        public void setLeader(String leader) {
            this.leader = leader;
        }
        public String getUptime() {
            return uptime;
        }
        public void setUptime(String uptime) {
            this.uptime = uptime;
        }
        public String getStartTime() {
            return startTime;
        }
        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
        @Override
        public String toString() {
            return "LeaderInfo [leader=" + leader + "]";
        }
    }
}
