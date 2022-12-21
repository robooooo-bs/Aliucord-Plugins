package com.github.MrAn0nym;

import android.content.Context;
import android.widget.TextView;
import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.utils.ReflectUtils;
import com.discord.databinding.WidgetChannelsListItemChannelBinding;
import com.discord.databinding.WidgetHomeBinding;
import com.discord.widgets.channels.list.WidgetChannelsListAdapter.ItemChannelText;
import com.discord.widgets.channels.list.items.ChannelListItem;
import com.discord.widgets.home.WidgetHome;
import com.discord.widgets.home.WidgetHomeHeaderManager;
import com.discord.widgets.home.WidgetHomeModel;

@AliucordPlugin
@SuppressWarnings("unused")
public class BetterDashless extends Plugin {
    public String filterString(String orig) {
		String basis = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890";

        char last = 'a';
        StringBuilder builder = new StringBuilder();
        for (char next : orig.toCharArray()) {
            if (basis.indexOf(next) == -1) {
                next = ' ';
            }
            
            if (last == ' ' && next == ' ') {
                continue;
            }
            
            builder.append(next);
            last = next;
        }

        String result = builder.toString();
        if (result.isEmpty() || result.equals(" ")) {
            return orig;
        } else {
            return result;
        }
    }
	
	@Override
	public void start(Context context) {
		
		patcher.patch(ItemChannelText.class, "onConfigure",
				new Class<?>[]{int.class, ChannelListItem.class}, new Hook(callFrame -> {
					ItemChannelText _this = (ItemChannelText) callFrame.thisObject;
					
					WidgetChannelsListItemChannelBinding binding = null;
					try {
						binding = (WidgetChannelsListItemChannelBinding) ReflectUtils
								.getField(_this, "binding");
					} catch (NoSuchFieldException | IllegalAccessException e) {
						e.printStackTrace();
					}
					
					assert binding != null;
					TextView channelName = binding.getRoot()
							.findViewById(Utils.getResId("channels_item_channel_name", "id"));
					
                    String name = filterString(channelName.getText().toString());
					channelName.setText(name);
				}));
		
		patcher.patch(WidgetHomeHeaderManager.class, "configure",
				new Class<?>[]{WidgetHome.class, WidgetHomeModel.class, WidgetHomeBinding.class},
				new Hook(callFrame -> {
					WidgetHomeHeaderManager _this = (WidgetHomeHeaderManager) callFrame.thisObject;
					WidgetHome widgetHome = (WidgetHome) callFrame.args[0];

					String orig = ((WidgetHomeModel) callFrame.args[1]).getChannel().m().toString();
                    String name = filterString(orig);
					widgetHome.setActionBarTitle(name);
				}));
	}
	
	@Override
	public void stop(Context context) {
		patcher.unpatchAll();
	}
}