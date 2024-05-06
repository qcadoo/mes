package com.qcadoo.mes.basic;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MultiUploadHelper {

    public static final List<String> EXTS = Lists
            .newArrayList("GIF", "JPG", "JPEG", "PNG", "PDF", "XLS", "XLSX", "DWG", "IPT", "IAM", "IDW", "DOC", "DOCX", "TXT",
                    "CSV", "XML", "ODT", "ODS", "TIFF", "TIF", "ZIP", "BTW", "DXF", "STP", "EZPX", "PLT");
}
