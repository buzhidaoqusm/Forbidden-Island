package com.island.controller;

import com.island.model.*;

public interface CardFactory {
    void initCards(CardController cardController, long seed);
}
