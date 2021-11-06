package com.yeahvin.pageProcessors;

import com.yeahvin.StartApplication;
import com.yeahvin.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yeahvin.StartApplication.DOWNLOAD_PATH;

/**
 * @author Asher
 * on 2021/11/6
 */
@Slf4j
public class NasaStarsProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(5).setSleepTime(1000).setTimeOut(10000);

    @Override
    public void process(Page page) {
        if (page.getUrl().regex(StartApplication.INDEX_PATH).match()){

            //获取文件夹下已经保存的文件
            File saveDir = new File(DOWNLOAD_PATH);
            List<String> fileNames = Arrays.stream(Optional.ofNullable(saveDir.listFiles()).orElse(new File[]{}))
                    .map(File::getName)
                    .collect(Collectors.toList());

            log.info("开始主页抓取");
            List<String> starsInfos = page.getRawText()
                    .lines()
                    .map(line -> {
                        List<String> infoList = StringUtil.getRegexString(line, "^\\d{4}.*<br>$");
                        boolean flag = infoList.size() > 0;
                        return flag ? infoList.get(0) : null;
                    })
                    .filter(StringUtils::isNoneBlank)
                    .collect(Collectors.toList());

            starsInfos.forEach(info -> {
                String link = StringUtil.getRegexString(info, "ap.*html").get(0);
                String name = StringUtil.getRegexString(info, ">.*</a>").get(0)
                        .replace("</a>", "")
                        .replace(">", "");

                //如果已经存在对应的文件，则进行跳过
                if (fileNames.stream().filter(fileName -> fileName.contains(name)).count() > 0) {
                    log.info("文件 [{}] 已经存在,进行跳过", name);
                    return;
                }
                page.addTargetRequest("https://apod.nasa.gov/apod/" + link);
            });
            log.info("完成主页抓取，一共有{}条数据", starsInfos.size());
        }else {
            page.getRawText()
                .lines()
                .filter(line -> line.contains("<title>")
                        || Pattern.compile("<a href=\"image.*jpg.*\">").matcher(line).find()
                        || Pattern.compile("<a href=\"image.*jpg.*\"").matcher(line).find()
                )
                .forEach(line -> {
                    if (line.contains("<title>")){
                        String date = StringUtil.getRegexString(line, "\\d{4}.*-").get(0)
                                .replace("-", "");
                        String name = StringUtil.getRegexString(line, "-.*").get(0)
                                .replace("-", "");
                        page.putField("date", date);
                        page.putField("name", name);
                    }else {
                        String imageLink = StringUtil.getRegexString(line, "image.*jpg").get(0);
                        page.putField("imageLink", imageLink);
                    }
                    page.putField("url", page.getUrl());
                });

        }
    }

    @Override
    public Site getSite() {
        return site;
    }
}
