package com.timelessname.watcher.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.timelessname.watcher.domain.ChannelData;
import com.timelessname.watcher.domain.Emoticon;
import com.timelessname.watcher.domain.EmoticonPrice;
import com.timelessname.watcher.domain.Message;
import com.google.common.*;

@Service
public class PricingService {

  @Resource
  Map<String, Emoticon> emoticonMap;

  @Resource
  List<String> emoticonList;

  @Value("${rabbit.queueName}")
  String queueName;

  @Value("${rabbit.exchangeName}")
  String exchangeName;

  @Value("${rabbit.channel.queueName}")
  String channelQueueName;

  @Value("${rabbit.channel.exchangeName}")
  String channelExchangeName;
  
  @Autowired
  RabbitTemplate rabbitTemplate;
  
  protected Map<String, List<Long>> emoticonTimes = new HashMap<String, List<Long>>();

  protected Map<String, Map<String, List<Long>>> channelMemeTimes = new HashMap<String, Map<String, List<Long>>>();

  @Autowired
  Gson gson;

  List<EmoticonPrice> emoticonPrices;

  List<ChannelData> channelDatas;

  public List<EmoticonPrice> getPrices() {
    return emoticonPrices;
  }
  
  public List<ChannelData> getChannelDatas() {
    return channelDatas;
  }

  @Scheduled(fixedDelay = 20)
  public void calculateStats() {

    long curTime;

    Map<String, Integer> emoticonCounts = new HashMap<String, Integer>();

    curTime = System.currentTimeMillis();
    synchronized (emoticonTimes) {
      for (String emoticonKey : emoticonTimes.keySet()) {
        List<Long> times = emoticonTimes.get(emoticonKey);
        int count = 0;
        for (Iterator<Long> iterator = times.iterator(); iterator.hasNext();) {
          long time = iterator.next();
          if (curTime - 60000 < time) {
            count++;
          } else {
            iterator.remove();
          }
        }
        emoticonCounts.put(emoticonKey, count);
      }
    }

    List<EmoticonPrice> prices = new ArrayList<EmoticonPrice>();
    for (String emoticonKey : emoticonCounts.keySet()) {
      prices.add(new EmoticonPrice(emoticonKey, emoticonCounts.get(emoticonKey)));
    }
    Collections.sort(prices);
    emoticonPrices = prices;
    

    //Type listType = new TypeToken<ArrayList<EmoticonPrice>>(){private static final long serialVersionUID = 1L;}.getType();
    //gson.fromJson(gson.toJson(emoticonPrices), listType);
    
    rabbitTemplate.convertAndSend(exchangeName, queueName, gson.toJson(emoticonPrices));

    List<ChannelData> data = new ArrayList<ChannelData>();

    synchronized (channelMemeTimes) {
      for (String channel : channelMemeTimes.keySet()) {
        Map<String, List<Long>> channelEmotes = channelMemeTimes.get(channel);
        TreeMap<Integer, String> priceToEmote = new TreeMap<Integer, String>();
        for (String emote : channelEmotes.keySet()) {
          List<Long> emoteTimes = channelEmotes.get(emote);
          int count = 0;
          for (Iterator<Long> iterator = emoteTimes.iterator(); iterator.hasNext();) {
            Long time = iterator.next();
            if (curTime - 60000 < time) {
              count++;
            } else {
              iterator.remove();
            }
          }
          priceToEmote.put(count, emote);

        }

        if (priceToEmote.descendingKeySet().size() > 0) {
          int p = priceToEmote.descendingKeySet().first();

          ChannelData channelData = new ChannelData();
          channelData.setChannel(channel);
          channelData.setTopEmoteCount(p);
          channelData.setTopEmote(priceToEmote.get(p));
          data.add(channelData);
        }

      }
    }
    Collections.sort(data, new Comparator<ChannelData>() {
      @Override
      public int compare(ChannelData o1, ChannelData o2) {
        return o2.getTopEmoteCount() - o1.getTopEmoteCount();
      }
    });

    channelDatas = data.subList(0, data.size() > 10 ? 10 : data.size());


    rabbitTemplate.convertAndSend(channelExchangeName, channelQueueName, gson.toJson(channelDatas));
    
  }

  public void receiveMessage(String json) {

    Message message = gson.fromJson(json, Message.class);

    String lmsg = message.getMessage().toLowerCase();

    for (String emoticon : emoticonList) {
      if (lmsg.contains(emoticon)) {

        if (emoticon.equals("gg")) {
          boolean valid = false;
          String[] parts = lmsg.split(" ");
          for (String string : parts) {
            if (emoticon.equals(string)) {
              valid = true;
              break;
            }
          }
          if(!valid){
            continue;
          }
        }
        
        synchronized (emoticonTimes) {
          List<Long> list = emoticonTimes.get(emoticon);
          if (list == null) {
            list = new ArrayList<Long>();
            emoticonTimes.put(emoticon, list);
          }
          list.add(System.currentTimeMillis());
        }

        synchronized (channelMemeTimes) {
          Map<String, List<Long>> chan = channelMemeTimes.get(message.getChannel());
          if (chan == null) {
            chan = new HashMap<String, List<Long>>();
            channelMemeTimes.put(message.getChannel(), chan);
          }
          List<Long> l = chan.get(emoticon);
          if (l == null) {
            l = new ArrayList<Long>();
            chan.put(emoticon, l);
          }
          l.add(System.currentTimeMillis());
        }

      }
    }

  }



}
