package com.zpj.mydownloader;

import com.zpj.downloader.util.FileUtil;

public class Utils {

    public static int getFileTypeIconId(String fileName) {
        FileUtil.FILE_TYPE fileType = FileUtil.checkFileType(fileName);
        if (fileType.equals(FileUtil.FILE_TYPE.TORRENT)) {
            return R.drawable.wechat_icon_bt;
        } else if (fileType.equals(FileUtil.FILE_TYPE.TXT)) {
            return R.drawable.wechat_icon_txt;
        } else if (fileType.equals(FileUtil.FILE_TYPE.APK)) {
            return R.drawable.wechat_icon_apk;
        } else if (fileType.equals(FileUtil.FILE_TYPE.PDF)) {
            return R.drawable.wechat_icon_pdf;
        } else if (fileType.equals(FileUtil.FILE_TYPE.DOC)) {
            return R.drawable.wechat_icon_word;
        } else if (fileType.equals(FileUtil.FILE_TYPE.PPT)) {
            return R.drawable.wechat_icon_ppt;
        } else if (fileType.equals(FileUtil.FILE_TYPE.XLS)) {
            return R.drawable.wechat_icon_excel;
        } else if (fileType.equals(FileUtil.FILE_TYPE.HTML)) {
            return R.drawable.wechat_icon_html;
        } else if (fileType.equals(FileUtil.FILE_TYPE.SWF)) {
            return R.drawable.format_flash;
        } else if (fileType.equals(FileUtil.FILE_TYPE.CHM)) {
            return R.drawable.format_chm;
        } else if (fileType.equals(FileUtil.FILE_TYPE.IMAGE)) {
            return R.drawable.format_picture;
        } else if (fileType.equals(FileUtil.FILE_TYPE.VIDEO)) {
            return R.drawable.format_media;
        } else if (fileType.equals(FileUtil.FILE_TYPE.ARCHIVE)) {
            return R.drawable.wechat_icon_zip;
        } else if (fileType.equals(FileUtil.FILE_TYPE.MUSIC)) {
            return R.drawable.wechat_icon_music;
        } else if (fileType.equals(FileUtil.FILE_TYPE.EBOOK)) {
            return R.drawable.wechat_icon_txt;
        }
        return R.drawable.wechat_icon_others;
    }
    
}
