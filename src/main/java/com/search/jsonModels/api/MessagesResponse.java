package com.search.jsonModels.api;

import com.search.jsonModels.Message;

import java.util.List;

public record MessagesResponse(long numResults, List<Message> results) { }
