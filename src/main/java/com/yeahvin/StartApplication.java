package com.yeahvin;

import com.yeahvin.pageProcessors.NasaStarsProcessor;
import com.yeahvin.pipelines.NasaStarsPipeline;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Spider;

/**
 * @author Asher
 * on 2021/11/6
 */
@Slf4j
public class StartApplication {

    //下载图片存储路径
    public static final String DOWNLOAD_PATH = "/Users/asher/gitWorkspace/temp/";
    public static final String INDEX_PATH = "https://apod.nasa.gov/apod/archivepix.html";

    public static void main(String[] args) {
        Spider.create(new NasaStarsProcessor())
                .addUrl(INDEX_PATH)
                .addPipeline(new NasaStarsPipeline())
                .run();
    }
}
