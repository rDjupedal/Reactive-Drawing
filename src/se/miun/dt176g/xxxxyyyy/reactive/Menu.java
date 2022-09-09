package se.miun.dt176g.xxxxyyyy.reactive;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;


/**
 * <h1>Menu</h1> 
 *
 * @author 	Rasmus Djupedal
 * @version 1.0
 * @since 	2022-09-08
 */
public class Menu extends JMenuBar {

	private static final long serialVersionUID = 1L;

	
	public Menu(MainFrame frame) {
		init(frame);
	}
	
	private void init(MainFrame frame) {
		
		JMenu menu;
		JMenuItem menuItem;

		menu = new JMenu("Some Menu category");
		this.add(menu);


		menuItem = new JMenuItem("Some menu item 1");
		menuItem.addActionListener(e -> anEvent(frame));
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Some menu item 2");
		menuItem.addActionListener(e ->  anotherEvent(frame));
		menu.add(menuItem);
	}

	private void anEvent(MainFrame frame) {
	
		String message = (String) JOptionPane.showInputDialog(frame,
				"Send message to everyone:");
		
		if(message != null && !message.isEmpty()) {
			JOptionPane.showMessageDialog(frame, message);
		}
	}
	
	private void anotherEvent(MainFrame frame) {
		
	}

}
