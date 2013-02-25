package me.botsko.prism.commands;

import java.util.List;

import org.bukkit.ChatColor;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionMessage;
import me.botsko.prism.actionlibs.QueryResult;
import me.botsko.prism.actions.Action;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.Flag;
import me.botsko.prism.commandlibs.SubHandler;
import me.botsko.prism.utils.TypeUtils;

public class PageCommand implements SubHandler {
	
	/**
	 * 
	 */
	private Prism plugin;
	
	
	/**
	 * 
	 * @param plugin
	 * @return 
	 */
	public PageCommand(Prism plugin) {
		this.plugin = plugin;
	}
	
	
	/**
	 * Handle the command
	 */
	public void handle(CallInfo call) {
		
		if(call.getArgs().length != 2){
			call.getPlayer().sendMessage( plugin.messenger.playerError( "Please specify a page number. Like /prism page 2" ) );
			return;
		}
		
		if(!TypeUtils.isNumeric( call.getArg(1) )){
			call.getPlayer().sendMessage( plugin.messenger.playerError( "Page numbers need to actually be numbers. Like /prism page 2" ) );
			return;
		}
		
		int page = Integer.parseInt(call.getArg(1));

		if(page <= 0){
			call.getPlayer().sendMessage( plugin.messenger.playerError("Page must be greater than zero.") );
			return;
		}
		
		// Is anything even cached?
		if(plugin.cachedQueries.containsKey(call.getPlayer().getName())){
			QueryResult results = plugin.cachedQueries.get(call.getPlayer().getName());
			results.setPage(page);
			
			// Refresh the query time and replace
			results.setQueryTime();
			plugin.cachedQueries.replace(call.getPlayer().getName(), results);
			
			// Results?
			if(!results.getActionResults().isEmpty()){
				call.getPlayer().sendMessage( plugin.messenger.playerHeaderMsg("Showing "+results.getTotal_results()+" results. Page "+page+" of "+results.getTotal_pages()) );
				List<Action> paginated = results.getPaginatedActionResults();
				if(paginated != null){
					for(Action a : paginated){
						ActionMessage am = new ActionMessage(a);
						if( results.getParameters().allowsNoRadius() || results.getParameters().hasFlag(Flag.EXTENDED) || plugin.getConfig().getBoolean("prism.always-show-extemded") ){
							am.showExtended();
						}
						call.getPlayer().sendMessage( plugin.messenger.playerMsg( am.getMessage() ) );
					}
				} else {
					call.getPlayer().sendMessage( plugin.messenger.playerError( "Pagination can't find anything. Do you have the right page number?" ) );
				}
			} else {
				call.getPlayer().sendMessage( plugin.messenger.playerError( "Nothing found." + ChatColor.GRAY + " Either you're missing something, or we are." ) );
			}
		} else {
			call.getPlayer().sendMessage( plugin.messenger.playerError( "There's no lookup results to show. They may have expired." ) );
		}
	}
}