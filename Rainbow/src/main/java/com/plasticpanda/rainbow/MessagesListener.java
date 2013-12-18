package com.plasticpanda.rainbow;

import java.util.List;

public interface MessagesListener extends SimpleListener {

    public void onSuccess(List<Message> data);
}
