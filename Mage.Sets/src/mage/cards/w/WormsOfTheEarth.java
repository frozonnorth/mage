/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.cards.w;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.common.BeginningOfUpkeepTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.costs.Cost;
import mage.abilities.costs.common.SacrificeTargetCost;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.ContinuousRuleModifyingEffectImpl;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.filter.common.FilterControlledLandPermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.events.GameEvent;
import mage.game.events.ZoneChangeEvent;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;

/**
 *
 * @author L_J
 */
public class WormsOfTheEarth extends CardImpl {

    public WormsOfTheEarth(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.ENCHANTMENT},"{2}{B}{B}{B}");

        // Players can't play lands.
        this.addAbility(new SimpleStaticAbility(Zone.BATTLEFIELD, new WormsOfTheEarthPlayEffect()));

        // Lands can't enter the battlefield.
        this.addAbility(new SimpleStaticAbility(Zone.BATTLEFIELD, new WormsOfTheEarthEnterEffect()));

        // At the beginning of each upkeep, any player may sacrifice two lands or have Worms of the Earth deal 5 damage to him or her. If a player does either, destroy Worms of the Earth.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(Zone.BATTLEFIELD, new WormsOfTheEarthDestroyEffect(), TargetController.ANY, false));
    }

    public WormsOfTheEarth(final WormsOfTheEarth card) {
        super(card);
    }

    @Override
    public WormsOfTheEarth copy() {
        return new WormsOfTheEarth(this);
    }
}

class WormsOfTheEarthPlayEffect extends ContinuousRuleModifyingEffectImpl {

    public WormsOfTheEarthPlayEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Neutral);
        this.staticText = "Players can't play lands";
    }
    
    public WormsOfTheEarthPlayEffect(final WormsOfTheEarthPlayEffect effect) {
        super(effect);
    }

    @Override
    public WormsOfTheEarthPlayEffect copy() {
        return new WormsOfTheEarthPlayEffect(this);
    }
    
    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.PLAY_LAND;
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        return true;
    }
}

class WormsOfTheEarthEnterEffect extends ContinuousRuleModifyingEffectImpl {

    public WormsOfTheEarthEnterEffect() {
        super(Duration.WhileOnBattlefield, Outcome.Benefit);
        staticText = "Lands can't enter the battlefield";
    }

    public WormsOfTheEarthEnterEffect(final WormsOfTheEarthEnterEffect effect) {
        super(effect);
    }

    @Override
    public WormsOfTheEarthEnterEffect copy() {
        return new WormsOfTheEarthEnterEffect(this);
    }

    @Override
    public boolean checksEventType(GameEvent event, Game game) {
        return GameEvent.EventType.ZONE_CHANGE == event.getType();
    }

    @Override
    public boolean applies(GameEvent event, Ability source, Game game) {
        ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
        if (zEvent.getToZone() == Zone.BATTLEFIELD) {
            Card card = game.getCard(zEvent.getTargetId());
            if (card != null && card.isLand()) {
                return true;
            }
        }
        return false;
    }
}

class WormsOfTheEarthDestroyEffect extends OneShotEffect {

    public WormsOfTheEarthDestroyEffect() {
        super(Outcome.Benefit);
        this.staticText = "any player may sacrifice two lands or have {this} deal 5 damage to him or her. If a player does either, destroy {this}";
    }

    public WormsOfTheEarthDestroyEffect(final WormsOfTheEarthDestroyEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent sourcePermanent = game.getPermanentOrLKIBattlefield(source.getSourceId());
        if (controller != null && sourcePermanent != null) {
            Cost cost = new SacrificeTargetCost(new TargetControlledPermanent(2, 2, new FilterControlledLandPermanent("two lands"), false));
            for (UUID playerId : game.getState().getPlayersInRange(controller.getId(), game)) {
                Player player = game.getPlayer(playerId);
                if (player != null) {
                    if (player.chooseUse(outcome, "Do you want to destroy " + sourcePermanent.getLogName() + "? (sacrifice two lands or have it deal 5 damage to you)", source, game)) {
                        cost.clearPaid();
                        if (cost.canPay(source, source.getSourceId(), player.getId(), game) 
                                && player.chooseUse(Outcome.Sacrifice, "Will you sacrifice two lands? (otherwise you'll be dealt 5 damage)", source, game)) {
                            if (!cost.pay(source, game, source.getSourceId(), player.getId(), false, null)) {
                                player.damage(5, source.getSourceId(), game, false, true);
                            }
                        } else {
                            player.damage(5, source.getSourceId(), game, false, true);
                        }
                        sourcePermanent = game.getPermanent(source.getSourceId());
                        if (sourcePermanent != null) {
                            sourcePermanent.destroy(source.getSourceId(), game, false);
                        }
                        break;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public WormsOfTheEarthDestroyEffect copy() {
        return new WormsOfTheEarthDestroyEffect(this);
    }
}
