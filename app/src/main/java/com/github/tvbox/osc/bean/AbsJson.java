package com.github.tvbox.osc.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pj567
 * @date :2020/12/18
 * @description:
 */
public class AbsJson implements Serializable {

    public int code;    // : 1
    public String limit;    // : "20"
    public ArrayList<AbsJsonVod> list; // : [{vod_id: 71930, type_id: 22, type_id_1: 20, group_id: 0, vod_name: "意式情歌",…},…]
    public String msg;  // : "数据列表"
    public int page;    // : "2"
    public int pagecount;   // : 209
    public int total;   // : 4166


    public class AbsJsonVod implements Serializable {
        public int group_id; //: 0
        public int type_id; //: 32
        public int type_id_1; //: 31
        public String type_name; //: "国产剧"
        public String vod_actor; //: "黄小戈,赵旭东,时男,傅隽,张佳琳"
        public String vod_area; //: "中国大陆"
        public String vod_author; //: ""
        public String vod_behind; //: ""
        public String vod_blurb; //: "本剧通过讲述咸鱼馆神秘店主灵叔以咸鱼和rose为主角虚构的十六种不同人生故事，展现了在不同故事中的咸鱼和rose犹如千千万万生活在世界上的青年男女一样，拥有着不同的性格和不同背景，怀揣着不同梦想和欲望"
        public String vod_class; //: "剧情,爱情,科幻,悬疑,惊悚,国产剧"
        public String vod_color; //: ""
        public String vod_content; //: "<p>本剧通过讲述咸鱼馆神秘店主灵叔以咸鱼和rose为主角虚构的十六种不同人生故事，展现了在不同故事中的咸鱼和rose犹如千千万万生活在世界上的青年男女一样，拥有着不同的性格和不同背景，怀揣着不同梦想和欲望，在充满机遇挑战又布满荆棘的人生旅途中，积极面对人生困惑，努力走出困惑和绝境的故事。十六个小故事有神奇，有烦恼，有笑声，有感动，对爱情的向外，对亲情的追忆，对人生的徘徊，对未来的恐惧，构成了一幅咸鱼和rose生命多种可能性的美丽画卷，犹如人生百味，柴米油盐酱醋茶，酸甜苦辣咸，喜怒哀乐怨。人生总是在希望中面临困惑走向绝望，又从绝望中坚强不息走向希望，故事中咸鱼和rose心底善良，拼搏向上，于人生困境中搏出一片青天，体验了生命的各种美好，传递了人生应该坚持希望，积极向上拥抱美好的乐观精神，表达了生命不止，自强不息的内涵思想。</p>"
        public String vod_copyright; //: 0
        public String vod_director; //: "王凯阳,Kaiyang,Wang"
        public String vod_douban_id; //: 35373052
        public String vod_douban_score; //: "4.4"
        public String vod_down; //: 0
        public String vod_down_from; //: ""
        public String vod_down_note; //: ""
        public String vod_down_server; //: ""
        public String vod_down_url; //: ""
        public String vod_duration; //: "10"
        public String vod_en; //: "xianyuxianshengRosexiaojiezhihuixinglailiao"
        public String vod_hits; //: 0
        public String vod_hits_day; //: 0
        public String vod_hits_month; //: 0
        public String vod_hits_week; //: 0
        public String vod_id; //: 71989
        public String vod_isend; //: 0
        public String vod_jumpurl; //: ""
        public String vod_lang; //: "汉语普通话"
        public String vod_letter; //: "X"
        public String vod_level; //: 0
        public String vod_lock; //: 0
        public String vod_name; //: "咸鱼先生，Rose小姐之彗星来了"
        public String vod_pic; //: "https://img.52swat.cn/upload/vod/20210410-1/c8a9342fff893c85e4a255da90fdbe3f.jpg"
        public String vod_pic_screenshot; //: null
        public String vod_pic_slide; //: ""
        public String vod_pic_thumb; //: ""
        public String vod_play_from; //: "dbyun$$$dbm3u8"
        public String vod_play_note; //: "$$$"
        public String vod_play_server; //: "no$$$no"
        public String vod_play_url; //: "第01集$https://vod3.buycar5.cn/share/dHsXTOBwbaX4idZb#第02集$https://vod3.buycar5.cn/share/qTlFmVkS3ABl7F4v#第03集$https://vod3.buycar5.cn/share/uNAQVhnro4Xnx4Y1#第04集$https://vod3.buycar5.cn/share/EtGK2XPmuzygMFmE#第05集$https://vod3.buycar5.cn/share/MC1U1bcQrGgVxF6h#第06集$https://vod3.buycar5.cn/share/gEtYSq6IX9KWPykl#第07集$https://vod3.buycar5.cn/share/OEMBq5ujsPaq8Sy7#第08集$https://vod3.buycar5.cn/share/bynmQTMBQwsVHtkn#第09集$https://vod3.buycar5.cn/share/Th7aQDVPOT1p6Cib#第10集$https://vod3.buycar5.cn/share/8AaZzRvh3fFk43Mi#第11集$https://vod3.buycar5.cn/share/YzEk819PQphuqDgL#第12集$https://vod3.buycar5.cn/share/vdAGJhlSg0o1yzcA$$$第01集$https://vod3.buycar5.cn/20210410/iWay2ycC/index.m3u8#第02集$https://vod3.buycar5.cn/20210410/5DpcrSCA/index.m3u8#第03集$https://vod3.buycar5.cn/20210410/wVdGBPgj/index.m3u8#第04集$https://vod3.buycar5.cn/20210410/cUVpM93e/index.m3u8#第05集$https://vod3.buycar5.cn/20210410/NWALmXkH/index.m3u8#第06集$https://vod3.buycar5.cn/20210410/lXZKFL7d/index.m3u8#第07集$https://vod3.buycar5.cn/20210411/3gQEOdxL/index.m3u8#第08集$https://vod3.buycar5.cn/20210411/yMLR7Fsz/index.m3u8#第09集$https://vod3.buycar5.cn/20210411/vMtFz4in/index.m3u8#第10集$https://vod3.buycar5.cn/20210412/EOwKfgwt/index.m3u8#第11集$https://vod3.buycar5.cn/20210412/xRT9FEjR/index.m3u8#第12集$https://vod3.buycar5.cn/20210412/Q6krcXYC/index.m3u8"
        public String vod_plot; //: 0
        public String vod_plot_detail; //: ""
        public String vod_plot_name; //: ""
        public String vod_points; //: 0
        public String vod_points_down; //: 0
        public String vod_points_play; //: 0
        public String vod_pubdate; //: "2021-04-10(中国大陆)"
        public String vod_pwd; //: ""
        public String vod_pwd_down; //: ""
        public String vod_pwd_down_url; //: ""
        public String vod_pwd_play; //: ""
        public String vod_pwd_play_url; //: ""
        public String vod_pwd_url; //: ""
        public String vod_rel_art; //: ""
        public String vod_rel_vod; //: ""
        public String vod_remarks; //: "共30集,更新至12集"
        public String vod_reurl; //: ""
        public String vod_score; //: "4.4"
        public String vod_score_all; //: 460
        public String vod_score_num; //: 291
        public String vod_serial; //: "12"
        public String vod_state; //: ""
        public String vod_status; //: 1
        public String vod_sub; //: "Mr.Salted Fish Miss Ross 2,咸鱼先生，Rose小姐 第二季,咸鱼先生，Rose小姐之彗星来了"
        public String vod_tag; //: ""
        public String vod_time; //: "2021-04-12 19:13:27"
        public String vod_time_add; //: 1618053726
        public String vod_time_hits; //: 0
        public String vod_time_make; //: 0
        public String vod_total; //: 30
        public String vod_tpl; //: ""
        public String vod_tpl_down; //: ""
        public String vod_tpl_play; //: ""
        public String vod_trysee; //: 0
        public String vod_tv; //: ""
        public String vod_up; //: 0
        public String vod_version; //: ""
        public String vod_weekday; //: ""
        public String vod_writer; //: "周炎青,刘恒,支雅雪,孙露军,李璐,王梦璇"
        public String vod_year; //: "2021"

        public Movie.Video toXmlVideo() {
            Movie.Video video = new Movie.Video();
            video.last = vod_time;
            video.id = vod_id;
            video.tid = type_id;
            video.name = vod_name;
            video.type = type_name;
            // video.dt = vod_play_from == null ? "" : vod_play_from.replace("$$$", ",");
            video.pic = vod_pic;
            video.lang = vod_lang;
            video.area = vod_area;
            try {
                video.year = Integer.parseInt(vod_year);
            } catch (Throwable th) {
                video.year = 0;
            }
            video.state = vod_state;
            video.note = vod_remarks;
            video.actor = vod_actor;
            video.director = vod_director;
            Movie.Video.UrlBean urlBean = new Movie.Video.UrlBean();
            if (vod_play_from != null && vod_play_url != null) {
                String[] playFlags = vod_play_from.split("\\$\\$\\$");
                String[] playUrls = vod_play_url.split("\\$\\$\\$");
                List<Movie.Video.UrlBean.UrlInfo> infoList = new ArrayList<>();
                for (int i = 0; i < playFlags.length; i++) {
                    Movie.Video.UrlBean.UrlInfo urlInfo = new Movie.Video.UrlBean.UrlInfo();
                    urlInfo.flag = playFlags[i];
                    if (i < playUrls.length)
                        urlInfo.urls = playUrls[i];
                    else
                        urlInfo.urls = "";
                    infoList.add(urlInfo);
                }
                urlBean.infoList = infoList;
            }
            video.urlBean = urlBean;
            video.des = vod_content;// <![CDATA[权来]
            return video;
        }
    }

    public AbsXml toAbsXml() {
        AbsXml xml = new AbsXml();
        Movie movie = new Movie();
        movie.page = page;
        movie.pagecount = pagecount;
        try {
            movie.pagesize = Integer.parseInt(limit);
        } catch (Throwable th) {
            movie.pagesize = 0;
        }
        movie.recordcount = total;
        List<Movie.Video> videoList = new ArrayList<>();
        for (AbsJsonVod vod : list) {
            try {
                videoList.add(vod.toXmlVideo());
            } catch (Throwable th) {
                movie.pagesize = 0;
            }
        }
        movie.videoList = videoList;
        xml.movie = movie;
        return xml;
    }
}