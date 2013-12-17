package com.plasticpanda.rainbow;

import java.util.List;

/**
 * @author Luca Casartelli
 */

public interface Command {

    public void execute(List<Message> messages);
}
