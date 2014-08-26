package com.timelessname.watcher.domain;

public class EmoticonPrice implements Comparable<EmoticonPrice> {

  String emoticon;
  
  int perMinute;
  
  public EmoticonPrice(){
    
  }

  public EmoticonPrice(String emoticonKey, Integer count) {
    this.emoticon = emoticonKey;
    this.perMinute = count;
  }

  public String getEmoticon() {
    return emoticon;
  }

  public void setEmoticon(String emoticon) {
    this.emoticon = emoticon;
  }

  public int getPerMinute() {
    return perMinute;
  }

  public void setPerMinute(int perMinute) {
    this.perMinute = perMinute;
  }

  @Override
  public int compareTo(EmoticonPrice o) {
    return o.perMinute - this.perMinute;
  }
  
  
  
}
