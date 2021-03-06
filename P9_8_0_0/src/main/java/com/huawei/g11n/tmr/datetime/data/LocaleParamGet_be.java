package com.huawei.g11n.tmr.datetime.data;

import java.util.HashMap;

public class LocaleParamGet_be {
    public HashMap<String, String> date = new HashMap<String, String>() {
        {
            put("param_tmark", ":");
            put("param_am", "да\\s+паўдня|раніцы|апоўдні");
            put("param_pm", "пасля\\s+паўдня|вечара|поўдзень|апоўдні|вечарам");
            put("param_MMM", "сту|лют|сак|кра|мая|чэр|ліп|жні|вер|кас|ліс|сне");
            put("param_MMMM", "студзеня|лютага|сакавіка|красавіка|траўня|чэрвеня|ліпеня|жніўня|верасня|кастрычніка|лістапада|снежня");
            put("param_E", "\\bнд\\b|\\bпн\\b|\\bаў\\b|\\bср\\b|\\bчц\\b|\\bпт\\b|\\bсб\\b");
            put("param_E2", "нядзелю|панядзелка|\\bаў\\b|сераду|\\bчц\\b|пятніцу|суботу");
            put("param_EEEE", "нядзеля|панядзелак|аўторак|серада|чацвер|пятніца|субота");
            put("param_days", "сёння|заўтра|паслязаўтра");
            put("param_thisweek", "гэту\\s+нядзелю|гэту\\s+панядзелак|гэту\\s+аўторак|гэту\\s+сераду|гэту\\s+чацвер|гэту\\s+пятніцу|гэту\\s+суботу");
            put("param_nextweek", "наступную\\s+нядзелю|наступную\\s+панядзелак|наступную\\s+аўторак|наступную\\s+сераду|наступную\\s+чацвер|наступную\\s+пятніцу|наступную\\s+суботу");
            put("param_DateTimeBridge", "|\\bу\\b|\\bў\\b");
        }
    };
}
