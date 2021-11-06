package com.yeahvin.pipelines;

import com.yeahvin.utils.X509TrustManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.File;

import static com.yeahvin.StartApplication.DOWNLOAD_PATH;

/**
 * @author Asher
 * on 2021/11/6
 */
@Slf4j
public class NasaStarsPipeline implements Pipeline {

    @Override
    public void process(ResultItems resultItems, Task task) {
        if (resultItems.getAll()!=null && resultItems.getAll().size()>0){
            String date = resultItems.get("date");
            String name = resultItems.get("name");
            String imageLink = resultItems.get("imageLink");

            if (StringUtils.isBlank(imageLink)){
                log.error("获取图片下载链接失败，name:{}, date:{}, url:{}", name, date, resultItems.get("url"));
                return;
            }

            String fileName = date + name + ".jpg";
            if (new File(DOWNLOAD_PATH + fileName).exists()) {
                log.info("下载图片{}已经存在，跳过，name:{}, date:{}", imageLink, name, date);
                return;
            }
            try {
                X509TrustManager.downLoadFromUrlHttps("https://apod.nasa.gov/apod/" + imageLink, fileName, DOWNLOAD_PATH);
            } catch (Exception e) {
                log.error("图片下载失败：", e);
                e.printStackTrace();
            }
            log.info("下载图片{}完成，name:{}, date:{}", imageLink, name, date);
        }
    }

}

