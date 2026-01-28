package com.example.trainbrella.service;

import com.example.trainbrella.model.WeatherResult;
import com.example.trainbrella.model.HourlyWeather;
import org.springframework.stereotype.Service;

@Service
public class RiskService {

    // 遅延リスクを 0〜100 で返す
    public int calculate(WeatherResult w, String trainStatus) {

        int risk = 0;

        int maxPop = w.pop;               // 降水確率（%）
        double maxWind = w.windSpeed;     // 風速（m/s）
        double maxRain = 0.0;             // 雨量（mm）
        boolean thunder = false;
        boolean snow = false;
        boolean fog = false;

        // ===== 6時間先までの予報を見る =====
        if (w.hourly != null && !w.hourly.isEmpty()) {
            int hours = Math.min(6, w.hourly.size());
            for (int i = 0; i < hours; i++) {
                HourlyWeather h = w.hourly.get(i);

                if (h.pop > maxPop) {
                    maxPop = h.pop;
                }
                if (h.windSpeed > maxWind) {
                    maxWind = h.windSpeed;
                }
                if (h.rainAmount > maxRain) {
                    maxRain = h.rainAmount;
                }
                String desc = h.description != null ? h.description : "";
                if (desc.contains("雷")) thunder = true;
                if (desc.contains("雪") || desc.contains("みぞれ")) snow = true;
                if (desc.contains("霧") || desc.contains("もや") || desc.contains("濃霧")) fog = true;
            }
        }

        String nowDesc = w.description != null ? w.description : "";
        if (nowDesc.contains("雷")) thunder = true;
        if (nowDesc.contains("雪") || nowDesc.contains("みぞれ")) snow = true;
        if (nowDesc.contains("霧") || nowDesc.contains("もや") || nowDesc.contains("濃霧")) fog = true;

        // ===== 降水確率（メイン要因）=====
        if (maxPop >= 70) {
            risk += 20;
        } else if (maxPop >= 40) {
            risk += 10;
        }

        // ===== 風速（強風のみ影響）=====
        if (maxWind >= 15) {
            risk += 15;
        } else if (maxWind >= 10) {
            risk += 5;
        }

        // ===== 雨量（小さめ加点）=====
        if (maxRain >= 5.0) {
            risk += 8;
        } else if (maxRain >= 1.0) {
            risk += 4;
        }

        // ===== 雪・雷・濃霧（少し大きめ加点）=====
        if (snow) risk += 8;
        if (thunder) risk += 10;
        if (fog) risk += 10;

        // ===== すでに遅延・運休がある場合 =====
        if (trainStatus != null &&
           (trainStatus.contains("遅延")
         || trainStatus.contains("見合わせ")
         || trainStatus.contains("運休"))) {
            risk += 25;
        }

        // ===== 最大100で打ち止め =====
        return Math.min(risk, 100);
    }
}
