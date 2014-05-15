/*
 * This file is part of Arduino.
 *
 * Copyright 2014 Arduino LLC (http://www.arduino.cc/)
 *
 * Arduino is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, you may use this file as part of a free software
 * library without restriction.  Specifically, if other files instantiate
 * templates or use macros or inline functions from this file, or you compile
 * this file and link it with other files to produce an executable, this
 * file does not by itself cause the resulting executable to be covered by
 * the GNU General Public License.  This exception does not however
 * invalidate any other reasons why the executable file might be covered by
 * the GNU General Public License.
 */
package cc.arduino.packages.contributions.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import cc.arduino.packages.contributions.ContributedPackage;
import cc.arduino.packages.contributions.ContributedPlatform;
import cc.arduino.packages.contributions.ContributionsIndex;

@SuppressWarnings("serial")
public class ContributionIndexTableModel extends AbstractTableModel {

  public final static int DESCRIPTION_COL = 0;
  public final static int VERSION_COL = 1;
  public final static int INSTALLED_COL = 2;

  public static class ContributedPlatformReleases {
    public ContributedPackage packager;
    public String arch;
    public List<ContributedPlatform> releases = new ArrayList<ContributedPlatform>();
    public List<String> versions = new ArrayList<String>();
    public ContributedPlatform selected = null;

    public ContributedPlatformReleases(ContributedPlatform platform) {
      packager = platform.getParentPackage();
      arch = platform.getArchitecture();
      add(platform);
    }

    public boolean shouldContain(ContributedPlatform platform) {
      if (platform.getParentPackage() != packager)
        return false;
      if (!platform.getArchitecture().equals(arch))
        return false;
      return true;
    }

    public void add(ContributedPlatform platform) {
      releases.add(platform);
      versions.add(platform.getVersion());
      selected = getLatest();
    }

    public ContributedPlatform getInstalled() {
      for (ContributedPlatform plat : releases)
        if (plat.isInstalled())
          return plat;
      return null;
    }

    public ContributedPlatform getLatest() {
      ContributedPlatform latest = null;
      for (ContributedPlatform plat : releases) {
        if (latest == null)
          latest = plat;
        if (plat.getVersion().compareTo(latest.getVersion()) > 0)
          latest = plat;
      }
      return latest;
    }

    public ContributedPlatform getSelected() {
      return selected;
    }

    public void selectVersion(String version) {
      for (ContributedPlatform plat : releases) {
        if (plat.getVersion().equals(version)) {
          selected = plat;
          return;
        }
      }
    }
  }

  private List<ContributedPlatformReleases> contributions = new ArrayList<ContributedPlatformReleases>();

  private String[] m_colNames = { "Description", "Available", "Installed" };

  private Class<?>[] m_colTypes = { ContributedPlatform.class, Object[].class,
      String.class };

  public void updateIndex(ContributionsIndex index) {
    contributions.clear();
    for (ContributedPackage pack : index.getPackages()) {
      for (ContributedPlatform platform : pack.getPlatforms()) {
        addContribution(platform);
      }
    }
  }

  private void addContribution(ContributedPlatform platform) {
    for (ContributedPlatformReleases contribution : contributions) {
      if (!contribution.shouldContain(platform))
        continue;
      contribution.add(platform);
      return;
    }

    contributions.add(new ContributedPlatformReleases(platform));
  }

  @Override
  public int getColumnCount() {
    return m_colNames.length;
  }

  @Override
  public int getRowCount() {
    return contributions.size();
  }

  @Override
  public String getColumnName(int column) {
    return m_colNames[column];
  }

  @Override
  public Class<?> getColumnClass(int col) {
    return m_colTypes[col];
  }

  @Override
  public void setValueAt(Object value, int row, int col) {
    if (col == VERSION_COL) {
      contributions.get(row).selectVersion((String) value);
      fireTableCellUpdated(row, col);
    }
  }

  @Override
  public Object getValueAt(int row, int col) {
    ContributedPlatformReleases contribution = contributions.get(row);
    ContributedPlatform installed = contribution.getInstalled();
    if (col == DESCRIPTION_COL) {
      return contribution.getSelected();
    }
    if (col == VERSION_COL) {
      return contribution.getSelected().getVersion();
    }
    if (col == INSTALLED_COL) {
      return installed == null ? "-" : installed.getVersion();
    }
    return null;
  }

  @Override
  public boolean isCellEditable(int row, int col) {
    return col == VERSION_COL || col == INSTALLED_COL;
  }

  public List<String> getReleasesVersions(int row) {
    return contributions.get(row).versions;
  }

  public ContributedPlatformReleases getReleases(int row) {
    return contributions.get(row);
  }

  public ContributedPlatform getSelectedRelease(int row) {
    return contributions.get(row).getSelected();
  }

}