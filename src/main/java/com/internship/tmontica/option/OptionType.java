package com.internship.tmontica.option;

import lombok.Getter;

@Getter
public enum OptionType {

    Temperature{
        public StringBuilder attachString(StringBuilder sb, String amount ,Option option){
            return sb.append(option.getName());
        }
    },
    Shot{
        public StringBuilder attachString(StringBuilder sb, String amount ,Option option){
            return sb.append("/샷추가("+amount+"개)");
        }
    },
    Syrup{
        public StringBuilder attachString(StringBuilder sb, String amount ,Option option){
            return sb.append("/시럽추가("+amount+"개)");
        }
    },
    Size{
        public StringBuilder attachString(StringBuilder sb, String amount ,Option option){
            return sb.append("/사이즈업");
        }
    };

    public abstract StringBuilder attachString(StringBuilder sb, String amount ,Option option);


}
