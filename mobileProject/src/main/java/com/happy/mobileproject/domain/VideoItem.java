package com.happy.mobileproject.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhonglq on 2015/11/4.
 */
public class VideoItem implements Serializable {


    /**
     * cName : 不生病的秘密
     * list : [{"cId":"1","cName":"不生病的秘密","tId":"3","tName":"陈咏骐","vContent":"1.五脏功能2.生病真因3.中医养生","vDownload":"0","vFullLong":"34","vHeadUrl":"Public/Upload/2015-09/Video//1-150927162312_1.jpg","vId":"1","vIsFee":"0","vName":"不生病的秘密","vNoFullLong":"0","vNum":"0","vPlayCount":"0"},{"cId":"1","cName":"不生病的秘密","tId":"3","tName":"陈咏骐","vContent":"1不病秘籍2西医分类认识3中医养生认识4调养与食养","vDownload":"0","vFullLong":"56","vHeadUrl":"Public/Upload/2015-09/Video//1-150927162526_1.jpg","vId":"2","vIsFee":"1","vName":"人体\u201c不癌\u201d的预防与食养","vNoFullLong":"15","vNum":"0","vPlayCount":"0"},{"cId":"1","cName":"不生病的秘密","tId":"2","tName":"赖易锋","vContent":"1不病秘籍2西医分类认识3中医养生认识4调养与食养","vDownload":"0","vFullLong":"120","vHeadUrl":"Public/Upload/2015-09/Video//1-150927162854_1.jpg","vId":"3","vIsFee":"1","vName":"慢性病的预防与食养","vNoFullLong":"20","vNum":"0","vPlayCount":"0"}]
     * msg : 未找到
     * result : 0
     * tName : 陈咏骐
     * vHeadUrl : Public/Upload/2015-09/Video//1-150927162312_1.jpg
     * vName : 不生病的秘密
     * vPlayAddr : http://www.qiuzhimen.com/Public/video/1.mp4
     * vRedirect : http://www.qiuzhimen.com/app.html
     */

    private String cName;//课程名
    private String msg;
    private int result;
    private String tName;//老师名
    private String vHeadUrl;//视频缩略图
    private String vName;//视频名
    private String vPlayAddr;//播放地址
    private String vRedirect;//跳转地址
    /**
     * cId : 1
     * cName : 不生病的秘密
     * tId : 3
     * tName : 陈咏骐
     * vContent : 1.五脏功能2.生病真因3.中医养生
     * vDownload : 0
     * vFullLong : 34
     * vHeadUrl : Public/Upload/2015-09/Video//1-150927162312_1.jpg
     * vId : 1
     * vIsFee : 0
     * vName : 不生病的秘密
     * vNoFullLong : 0
     * vNum : 0
     * vPlayCount : 0
     */

    private List<ListEntity> list;

    public void setCName(String cName) {
        this.cName = cName;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public void setTName(String tName) {
        this.tName = tName;
    }

    public void setVHeadUrl(String vHeadUrl) {
        this.vHeadUrl = vHeadUrl;
    }

    public void setVName(String vName) {
        this.vName = vName;
    }

    public void setVPlayAddr(String vPlayAddr) {
        this.vPlayAddr = vPlayAddr;
    }

    public void setVRedirect(String vRedirect) {
        this.vRedirect = vRedirect;
    }

    public void setList(List<ListEntity> list) {
        this.list = list;
    }

    public String getCName() {
        return cName;
    }

    public String getMsg() {
        return msg;
    }

    public int getResult() {
        return result;
    }

    public String getTName() {
        return tName;
    }

    public String getVHeadUrl() {
        return vHeadUrl;
    }

    public String getVName() {
        return vName;
    }

    public String getVPlayAddr() {
        return vPlayAddr;
    }

    public String getVRedirect() {
        return vRedirect;
    }

    public List<ListEntity> getList() {
        return list;
    }

    public static class ListEntity implements Serializable {
        private String cId;
        private String cName;
        private String tId;
        private String tName;
        private String vContent;
        private String vDownload;
        private String vFullLong;
        private String vHeadUrl;
        private String vId;
        private String vIsFee;
        private String vName;
        private String vNoFullLong;
        private String vNum;
        private String vPlayCount;

        public void setCId(String cId) {
            this.cId = cId;
        }

        public void setCName(String cName) {
            this.cName = cName;
        }

        public void setTId(String tId) {
            this.tId = tId;
        }

        public void setTName(String tName) {
            this.tName = tName;
        }

        public void setVContent(String vContent) {
            this.vContent = vContent;
        }

        public void setVDownload(String vDownload) {
            this.vDownload = vDownload;
        }

        public void setVFullLong(String vFullLong) {
            this.vFullLong = vFullLong;
        }

        public void setVHeadUrl(String vHeadUrl) {
            this.vHeadUrl = vHeadUrl;
        }

        public void setVId(String vId) {
            this.vId = vId;
        }

        public void setVIsFee(String vIsFee) {
            this.vIsFee = vIsFee;
        }

        public void setVName(String vName) {
            this.vName = vName;
        }

        public void setVNoFullLong(String vNoFullLong) {
            this.vNoFullLong = vNoFullLong;
        }

        public void setVNum(String vNum) {
            this.vNum = vNum;
        }

        public void setVPlayCount(String vPlayCount) {
            this.vPlayCount = vPlayCount;
        }

        public String getCId() {
            return cId;
        }

        public String getCName() {
            return cName;
        }

        public String getTId() {
            return tId;
        }

        public String getTName() {
            return tName;
        }

        public String getVContent() {
            return vContent;
        }

        public String getVDownload() {
            return vDownload;
        }

        public String getVFullLong() {
            return vFullLong;
        }

        public String getVHeadUrl() {
            return vHeadUrl;
        }

        public String getVId() {
            return vId;
        }

        public String getVIsFee() {
            return vIsFee;
        }

        public String getVName() {
            return vName;
        }

        public String getVNoFullLong() {
            return vNoFullLong;
        }

        public String getVNum() {
            return vNum;
        }

        public String getVPlayCount() {
            return vPlayCount;
        }
    }
}
