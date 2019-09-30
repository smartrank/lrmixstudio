/**
 * Copyright (C) 2013, 2014 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.lrmixstudio.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleWriter {

    public static final Logger LOG = LoggerFactory.getLogger(SampleWriter.class);

    private SampleWriter() {
    }

    /**
     *
     * @param selectedFile
     * @param profileName
     * @param dataList
     * @throws IOException if the file exists but is a directory rather than a
     * regular file, does not exist but cannot be created, or cannot be opened
     * for any other reason, or there is an error writing to the file.
     *
     */
    public static void write(File selectedFile, String profileName, List<List<String>> dataList) throws IOException {
        FileOutputStream fos = null;
        try {
            LOG.debug("Saving profile {} to {}", profileName, selectedFile);
            fos = new FileOutputStream(selectedFile);
            fos.write("SampleName,Marker,Allele1,Allele2,Allele3,Allele4,Allele5,Allele6,Allele7,Allele8\n".getBytes());
            for (List<String> rowData : dataList) {
                String locus = rowData.get(0);
                ArrayList<String> alleles = new ArrayList<>();
                for (String allele : rowData) {
                    if (allele != null && !allele.isEmpty() && !allele.equals(locus)) {
                        alleles.add(allele);
                    }
                }
                // Only write loci that have at least one allele filled in
                if (!alleles.isEmpty()) {
                    StringBuilder csv = new StringBuilder(profileName + "," + locus);
                    for (String a : alleles) {
                        csv.append(",").append(a);
                    }

                    fos.write((csv.append("\n")).toString().getBytes());
                }
            }
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
            }
        }
    }

}
