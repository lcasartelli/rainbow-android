package com.plasticpanda.rainbow;

import java.util.List;

public interface MessagesListener {

    public void onSuccess(List<Message> data);

    public void onError();
}
