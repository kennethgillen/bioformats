/*
 * #%L
 * Bio-Formats autogen package for programmatically generating source code.
 * %%
 * Copyright (C) 2007 - 2012 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;

import loci.common.DataTools;
import loci.common.IniList;
import loci.common.IniParser;
import loci.common.IniTable;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.UnknownFormatException;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/autogen/src/FormatPageAutogen.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/autogen/src/FormatPageAutogen.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Melissa Linkert melissa at glencoesoftware.com
 */
public class FormatPageAutogen {

  // -- Constants --

  private static final String TEMPLATE = "doc/FormatPage.vm";
  private static final String TABLE_TEMPLATE = "doc/FormatTable.vm";

  // -- Fields --

  private IniList data;

  // -- Constructor --

  public FormatPageAutogen(String dataFile) throws FormatException, IOException
  {
    String file = DataTools.readFile(dataFile);
    IniParser parser = new IniParser();
    parser.setCommentDelimiter(null);
    data = parser.parseINI(new BufferedReader(new StringReader(file)));
  }

  // -- API Methods --

  public void writeFormatPages() throws Exception {
    File doc = new File("../../docs/sphinx/loci/");
    if (!doc.exists()) {
      boolean success = doc.mkdir();
      if (!success) {
        throw new IOException("Could not create " + doc.getAbsolutePath());
      }
    }

    VelocityEngine engine = VelocityTools.createEngine();
    VelocityContext context = VelocityTools.createContext();

    for (IniTable table : data) {
      String format = table.get(IniTable.HEADER_KEY);
      context.put("format", format);
      context.put("extensions", table.get("extensions"));
      context.put("owner", table.get("owner"));
      context.put("developer", table.get("developer"));
      context.put("scifio", table.get("scifio"));
      context.put("export", table.get("export"));
      context.put("versions", table.get("versions"));
      context.put("pixelsRating", table.get("pixelsRating"));
      context.put("metadataRating", table.get("metadataRating"));
      context.put("opennessRating", table.get("opennessRating"));
      context.put("presenceRating", table.get("presenceRating"));
      context.put("utilityRating", table.get("utilityRating"));
      context.put("reader", table.get("reader"));
      context.put("writer", table.get("writer"));
      context.put("notes", table.get("notes"));
      context.put("privateSpecification", table.get("privateSpecification"));
      context.put("component",
        table.get("scifio").equals("no") ? "bio-formats" : "scifio");

      if (table.containsKey("software")) {
        String[] software = table.get("software").split("\n");
        context.put("software", software);
      }

      if (table.containsKey("weHave")) {
        String[] weHave = table.get("weHave").split("\n");
        context.put("weHave", weHave);
      }

      if (table.containsKey("weWant")) {
        String[] weWant = table.get("weWant").split("\n");
        context.put("weWant", weWant);
      }

      if (table.containsKey("samples")) {
        String[] samples = table.get("samples").split("\n");
        context.put("samples", samples);
      }

      if (table.containsKey("notes")) {
        String[] notes = table.get("notes").split("\n");
        context.put("notes", notes);
      }

      String filename = getPageName(format);
      VelocityTools.processTemplate(engine, context, TEMPLATE,
        "../../docs/sphinx/loci/" + filename + ".txt");
    }
  }

  public void writeFormatTable() throws Exception {
    File doc = new File("../../docs/sphinx/loci/");
    if (!doc.exists()) {
      boolean success = doc.mkdir();
      if (!success) {
        throw new IOException("Could not create " + doc.getAbsolutePath());
      }
    }

    VelocityEngine engine = VelocityTools.createEngine();
    VelocityContext context = VelocityTools.createContext();

    IniTable[] sortedTable = new IniTable[data.size()];
    for (int i=0; i<data.size(); i++) {
      IniTable table = data.get(i);
      table.put("pagename", getPageName(table.get(IniTable.HEADER_KEY)));
      sortedTable[i] = table;
    }

    Arrays.sort(sortedTable, new Comparator<IniTable>() {
      public int compare(IniTable t1, IniTable t2) {
        String page1 = t1.get("pagename");
        String page2 = t2.get("pagename");

        return page1.compareTo(page2);
      }
    });

    context.put("formats", sortedTable);
    context.put("count", sortedTable.length);

    VelocityTools.processTemplate(engine, context, TABLE_TEMPLATE,
      "../../docs/sphinx/loci/bio-formats-formats.txt");
  }

  // -- Helper methods --

  private String getPageName(String format) {
    String filename = format.replaceAll("/", "");
    filename = filename.replaceAll("\\(", "");
    filename = filename.replaceAll("\\)", "");
    filename = filename.replaceAll("\\.", "");
    filename = filename.replaceAll("& ", "");
    filename = filename.replaceAll(" ", "-");
    filename = "bio-formats-format-" + filename;
    filename = filename.toLowerCase();

    return filename;
  }

  // -- Main method --

  public static void main(String[] args) throws Exception {
    FormatPageAutogen autogen = new FormatPageAutogen(args[0]);
    autogen.writeFormatPages();
    autogen.writeFormatTable();
  }

}
