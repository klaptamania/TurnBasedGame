package com.klapt.utilityTool;

import com.badlogic.gdx.math.MathUtils;
import com.klapt.interfacePack.Gacha;

import java.util.List;

public class QuickGacha<T extends Gacha> {
    //SKills
    private final List<T> list;


    private final int[] cumulativeWeights;
    private final int totalWeight;

    public QuickGacha(List<T> list){
        this.list = list;
        cumulativeWeights = new int[list.size()];

        int currentSum = 0;
        for(int i = 0; i < list.size(); i++){
            currentSum += list.get(i).getWeight();
            cumulativeWeights[i] = currentSum;
        }
        totalWeight = currentSum;
    }
    public T roll(){
        int random = MathUtils.random(1, totalWeight);

        for(int i = 0; i < cumulativeWeights.length; i++){
            if(random <= cumulativeWeights[i]){
                return list.get(i);
            }
        }

        return list.getFirst();
    }
}
