package com.island.controller.factory;

import com.island.controller.CardController;

public interface CardFactory {
    void initCards(CardController cardController, long seed);
}
