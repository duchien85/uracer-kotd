
package com.bitfire.uracer.game.logic.gametasks;

import com.badlogic.gdx.utils.Array;
import com.bitfire.uracer.events.GameRendererEvent;
import com.bitfire.uracer.events.GameRendererEvent.Order;
import com.bitfire.uracer.events.GameRendererEvent.Type;
import com.bitfire.uracer.game.GameEvents;
import com.bitfire.uracer.game.logic.gametasks.hud.HudElement;
import com.bitfire.utils.ItemsManager;

/** Encapsulates an head-up manager that will callback HudElement events for their updating and drawing operations. */
public final class Hud extends GameTask {

	private static final GameRendererEvent.Type RenderEventBeforePost = GameRendererEvent.Type.BatchAfterMeshes;
	private static final GameRendererEvent.Type RenderEventAfterPost = GameRendererEvent.Type.BatchAfterPostProcessing;
	private static final GameRendererEvent.Type RenderEventDebug = GameRendererEvent.Type.BatchDebug;

	private final ItemsManager<HudElement> managerBeforePost = new ItemsManager<HudElement>();
	private final ItemsManager<HudElement> managerAfterPost = new ItemsManager<HudElement>();
	private final ItemsManager<HudElement> managerDebug = new ItemsManager<HudElement>();

	private final GameRendererEvent.Listener renderEvent = new GameRendererEvent.Listener() {
		@Override
		public void handle (Object source, Type type, Order order) {
			if (order != GameRendererEvent.Order.DEFAULT) {
				return;
			}

			if (type == Type.BatchAfterMeshes) {
				Array<HudElement> items = managerBeforePost.items;
				for (int i = 0; i < items.size; i++) {
					items.get(i).onRender(GameEvents.gameRenderer.batch);
				}
			} else if (type == Type.BatchAfterPostProcessing) {
				Array<HudElement> items = managerAfterPost.items;
				for (int i = 0; i < items.size; i++) {
					items.get(i).onRender(GameEvents.gameRenderer.batch);
				}
			} else if (type == Type.BatchDebug) {
				Array<HudElement> items = managerDebug.items;
				for (int i = 0; i < items.size; i++) {
					items.get(i).onRender(GameEvents.gameRenderer.batch);
				}
			}
		}
	};

	public Hud () {
		GameEvents.gameRenderer.addListener(renderEvent, RenderEventBeforePost, GameRendererEvent.Order.DEFAULT);
		GameEvents.gameRenderer.addListener(renderEvent, RenderEventAfterPost, GameRendererEvent.Order.DEFAULT);
		GameEvents.gameRenderer.addListener(renderEvent, RenderEventDebug, GameRendererEvent.Order.DEFAULT);
	}

	public void addBeforePostProcessing (HudElement element) {
		managerBeforePost.add(element);
	}

	public void addAfterPostProcessing (HudElement element) {
		managerAfterPost.add(element);
	}

	public void addDebug (HudElement element) {
		managerDebug.add(element);
	}

	public void remove (HudElement element) {
		managerBeforePost.remove(element);
		managerAfterPost.remove(element);
		managerDebug.remove(element);
	}

	@Override
	public void dispose () {
		super.dispose();
		GameEvents.gameRenderer.removeListener(renderEvent, RenderEventBeforePost, GameRendererEvent.Order.DEFAULT);
		GameEvents.gameRenderer.removeListener(renderEvent, RenderEventAfterPost, GameRendererEvent.Order.DEFAULT);
		GameEvents.gameRenderer.removeListener(renderEvent, RenderEventDebug, GameRendererEvent.Order.DEFAULT);
		managerBeforePost.dispose();
		managerAfterPost.dispose();
	}

	@Override
	public void onRestart () {
		for (int i = 0; i < managerBeforePost.items.size; i++) {
			managerBeforePost.items.get(i).onRestart();
		}

		for (int i = 0; i < managerAfterPost.items.size; i++) {
			managerAfterPost.items.get(i).onRestart();
		}

		for (int i = 0; i < managerDebug.items.size; i++) {
			managerDebug.items.get(i).onRestart();
		}
	}

	@Override
	public void onReset () {
		for (int i = 0; i < managerBeforePost.items.size; i++) {
			managerBeforePost.items.get(i).onReset();
		}

		for (int i = 0; i < managerAfterPost.items.size; i++) {
			managerAfterPost.items.get(i).onReset();
		}

		for (int i = 0; i < managerDebug.items.size; i++) {
			managerDebug.items.get(i).onReset();
		}
	}

	@Override
	protected void onTick () {
		for (int i = 0; i < managerBeforePost.items.size; i++) {
			managerBeforePost.items.get(i).onTick();
		}

		for (int i = 0; i < managerAfterPost.items.size; i++) {
			managerAfterPost.items.get(i).onTick();
		}

		for (int i = 0; i < managerDebug.items.size; i++) {
			managerDebug.items.get(i).onTick();
		}
	}
}
