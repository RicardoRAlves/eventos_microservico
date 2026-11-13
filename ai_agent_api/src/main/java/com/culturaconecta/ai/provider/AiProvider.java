package com.culturaconecta.ai.provider;

public interface AiProvider {

    String generate(String systemPrompt, String userPrompt);

}