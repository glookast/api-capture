package com.glookast.api.capture;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class SystemTestResult
{
    private Boolean storagePathExists;
    private Boolean storagePathIsFolder;
    private Boolean storagePathCanRead;
    private Boolean storagePathCanWrite;
    private Boolean storagePathCanExecute;
    private Boolean pamReachable;
    private Boolean pamRootFolderExists;
    private Boolean pamFolderExists;
}
