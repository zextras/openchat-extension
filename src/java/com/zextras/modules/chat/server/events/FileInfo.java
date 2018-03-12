package com.zextras.modules.chat.server.events;

import java.util.Objects;

public class FileInfo
{
  private final String mOwnerId;
  private final String mShareId;
  private final String mFilename;
  private final Long   mFilesize;
  private final String mContentType;

  public FileInfo(String ownerId, String shareId, String filename, long filesize, String contentType)
  {
    mOwnerId = ownerId;
    mShareId = shareId;
    mFilename = filename;
    mFilesize = filesize;
    mContentType = contentType;
  }

  public String getOwnerId()
  {
    return mOwnerId;
  }

  public String getShareId()
  {
    return mShareId;
  }

  public String getFilename()
  {
    return mFilename;
  }

  public Long getFilesize()
  {
    return mFilesize;
  }

  public String getContentType()
  {
    return mContentType;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }
    FileInfo fileInfo = (FileInfo) o;
    return Objects.equals(mOwnerId, fileInfo.mOwnerId) &&
      Objects.equals(mShareId, fileInfo.mShareId) &&
      Objects.equals(mFilename, fileInfo.mFilename) &&
      Objects.equals(mFilesize, fileInfo.mFilesize) &&
      Objects.equals(mContentType, fileInfo.mContentType);
  }

  @Override
  public int hashCode()
  {

    return Objects.hash(mOwnerId, mShareId, mFilename, mFilesize, mContentType);
  }
}
