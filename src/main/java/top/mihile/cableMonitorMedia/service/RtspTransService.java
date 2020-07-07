package top.mihile.cableMonitorMedia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.mihile.cableMonitorMedia.CableMonitorMediaServer;
import top.mihile.cableMonitorMedia.utils.RtspTransThread;
import top.mihile.cableMonitorMedia.utils.SystemCommandExecutor;
import top.mihile.cableMonitorMedia.utils.ThreadedStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mihile azuretodd@foxmail.com
 * @date 0272020/5/27
 */
@Service
public class RtspTransService {
    @Value("${server.port}")
    private Integer port;

    @Value("${mihile.ffmpegpath}")
    private String ffmpegpath;

    public static Map<UUID,Thread> ffmpegProcessMap = new ConcurrentHashMap<>();
    public static Map<UUID,Timer> ffmpegProcessTimerMap = new ConcurrentHashMap<>();

    public void doStartTrans(String url, UUID playChannel){
        List<String> commands = new ArrayList<String>();
        commands.add(ffmpegpath + "ffmpeg");
        commands.add("-stimeout");
        commands.add("5000000");
        commands.add("-rtsp_transport");
        commands.add("tcp");
//        commands.add("-re");
        commands.add("-i");
//        commands.add("\"rtsp://admin:mihile123@"+ip+":"+port+"/Streaming/Channels/102\"");
        commands.add(url);
        commands.add("-q");
        commands.add("0");
        commands.add("-f");
        commands.add("mpegts");
//        commands.add("-fflags");
//        commands.add("nobuffer");
        commands.add("-codec:v");
        commands.add("mpeg1video");

        //========  audio arguments 音频相关
        if(!CableMonitorMediaServer.audio){
            commands.add("-an");  // no audio 不能使音频记录,否则会大大降低转码效率，造成画面延迟卡顿
        }else {
            commands.add("-codec:a");
            commands.add("mp2");
            commands.add("-ar");
            commands.add("44100");
            commands.add("-ac");
            commands.add("1");
            commands.add("-b:a");
            commands.add("128k");
        }
        //========

        commands.add("-s");
        commands.add("640x480");
        commands.add("http://127.0.0.1:"+port+"/stream/upload/"+playChannel);
//        commands.add("ping");
//        commands.add("baidu.com");

        RtspTransThread rtspTransThread = new RtspTransThread(commands);
        rtspTransThread.start();

        registProcess(playChannel,rtspTransThread);
    }

    public  void registProcess(UUID playChannel,Thread thread){
        ffmpegProcessMap.put(playChannel,thread);
    }
}
