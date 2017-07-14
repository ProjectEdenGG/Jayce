package me.pugabear.jayce.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class Aliases
{
	public final HashMap<String, String> aliases = new HashMap<>();
	
	public Aliases() throws IOException
	{
		List<String> config = Files.readAllLines(Paths.get("Jayce" + FileSystems.getDefault().getSeparator() + "aliases.txt"));
		for (String line : config) 
		{
			String[] setting = line.split(" ");
			aliases.put(setting[0], setting[1]);
		}
	}
}
