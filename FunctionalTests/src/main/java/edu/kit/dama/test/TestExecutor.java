/*
 * Copyright 2014 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.test;

import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.Participant;
import edu.kit.dama.mdm.base.Relation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.Task;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.admin.client.impl.UserGroupRestClient;
import edu.kit.dama.rest.basemetadata.client.impl.BaseMetaDataRestClient;
import edu.kit.dama.rest.staging.client.impl.StagingRestClient;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.util.Constants;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.util.AdalapiSettings;
import edu.kit.lsdf.adalapi.util.ProtocolSettings;
import edu.kit.tools.url.URLCreator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic TestExecutor performing a KIT Data Manager functional test scenario.
 * This scenario covers:
 *
 * <ul>
 * <li>Creating of a base (administrative) metadata structure containing all
 * available entities.</li>
 * <li>Scheduling (20) ingests for all digital objects (one object per
 * investigation)</li>
 * <li>Performing transfer finalization for all ingests.</li>
 * <li>Data upload via WebDav.</li>
 * <li>Scheduling (20) downloads for all digital objects that where previously
 * created.</li>
 * <li>Performing transfer finalization for all downloads.</li>
 * <li>Data download via WebDav.</li>
 * <li>Loading all base metadata from the database.</li>
 * </ul>
 *
 * The tests are working best with the Docker setup located in the subfolder
 * ./Docker/KITDMTest/. If you want to reuse or modify the code please pay
 * attentions, that entities of the metadata graph are only updated in a few
 * cases (e.g. UserData, Tasks) when they are persisted. This means that you
 * should always query for an entity before you want to use it as it is not
 * guaranteed that all entity fields (e.g. the id) are already set to their
 * values stored in the database.
 *
 * @author mf6319
 */
public class TestExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestExecutor.class);

    private final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    //OUs
    private OrganizationUnit uFederation;
    private OrganizationUnit klingons;
    private OrganizationUnit vulcan;
    private OrganizationUnit betazoid;
    private OrganizationUnit borg;
    private OrganizationUnit talaxians;
    private OrganizationUnit ocampa;

    private Map<String, OrganizationUnit> allOrganizationUnits;
    //Persons
    private UserData jArcher;
    private UserData tPol;
    private UserData cTucker;
    private UserData mReed;
    private UserData hSato;
    private UserData tMayweather;
    private UserData jKirk;
    private UserData spock;
    private UserData mScott;
    private UserData lMcCoy;
    private UserData hSulu;
    private UserData nUhura;
    private UserData pChekov;
    private UserData jlPicard;
    private UserData wRiker;
    private UserData data;
    private UserData worf;
    private UserData nYar;
    private UserData bCrusher;
    private UserData dTroy;
    private UserData mOBrian;
    private UserData gLaForge;
    private UserData kJaneway;
    private UserData chakotay;
    private UserData tuvok;
    private UserData tParis;
    private UserData bTorres;
    private UserData hKim;
    private UserData tDoctor;
    private UserData soNine;
    private UserData neelix;
    private UserData kes;
    private UserData duras;
    private UserData surak;
    private UserData lTroi;
    private UserData borgQueen;

    private Map<String, UserData> allUsers;

//Studies and Investigations
    private Study nx1;
    private Investigation nx1_1;
    private Investigation nx1_2;
    private Investigation nx1_3;
    private Investigation nx1_4;
    private Investigation nx1_5;
    private Study ncc1701;
    private Investigation ncc1701_1;
    private Investigation ncc1701_2;
    private Investigation ncc1701_3;
    private Investigation ncc1701_4;
    private Investigation ncc1701_5;
    private Study ncc1701d;
    private Investigation ncc1701d_1;
    private Investigation ncc1701d_2;
    private Investigation ncc1701d_3;
    private Investigation ncc1701d_4;
    private Investigation ncc1701d_5;
    private Study ncc74656;
    private Investigation ncc74656_1;
    private Investigation ncc74656_2;
    private Investigation ncc74656_3;
    private Investigation ncc74656_4;
    private Investigation ncc74656_5;

    private Map<String, Study> allStudies;

    private Map<String, Investigation> allNX1Investigations;
    private Map<String, Investigation> allNCC1701Investigations;
    private Map<String, Investigation> allNCC1701DInvestigations;
    private Map<String, Investigation> allNCC74656Investigations;

    //Tasks
    private final Task captain = new Task("Captain");
    private final Task firstOfficer = new Task("First Officer");
    private final Task tacticalOfficer = new Task("Tactical Officer");
    private final Task scienceOfficer = new Task("Science Officer");
    private final Task helmsman = new Task("Helmsman");
    private final Task flightController = new Task("Flight Controller");
    private final Task navigator = new Task("Navigator");
    private final Task chiefEngineer = new Task("Chief Engineer");
    private final Task comOfficer = new Task("Communications Officer");
    private final Task operationsOfficer = new Task("Operations Officer");
    private final Task medicalOfficer = new Task("Chief Medical Officer");
    private final Task astroOfficer = new Task("Astrometrics Officer");
    private final Task moraleOfficer = new Task("Morale Officer");
    private final Task nurse = new Task("Nurse");
    private final Task counselor = new Task("Counselor");
    private Map<String, Task> allTasks;

    //MetadataSchemas
    private MetaDataSchema dc = new MetaDataSchema("dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
    private MetaDataSchema bmd = new MetaDataSchema("bmd", "http://datamanager.kit.edu/kdm/basemetadata/2012-04/");
    private Map<String, MetaDataSchema> allSchemas;

    /**
     * Create all metadata schemas.
     */
    private void createMetadataSchemas() {
        dc = new MetaDataSchema("dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        bmd = new MetaDataSchema("bmd", "http://datamanager.kit.edu/kdm/basemetadata/2012-04/");
        allSchemas = new HashMap<>();
        allSchemas.put(dc.getSchemaIdentifier(), dc);
        allSchemas.put(bmd.getSchemaIdentifier(), bmd);
    }

    /**
     * Create all tasks.
     */
    private void createTasks() {
        allTasks = new HashMap<>();
        allTasks.put(captain.getTask(), captain);
        allTasks.put(firstOfficer.getTask(), firstOfficer);
        allTasks.put(tacticalOfficer.getTask(), tacticalOfficer);
        allTasks.put(scienceOfficer.getTask(), scienceOfficer);
        allTasks.put(helmsman.getTask(), helmsman);
        allTasks.put(flightController.getTask(), flightController);
        allTasks.put(navigator.getTask(), navigator);
        allTasks.put(chiefEngineer.getTask(), chiefEngineer);
        allTasks.put(comOfficer.getTask(), comOfficer);
        allTasks.put(operationsOfficer.getTask(), operationsOfficer);
        allTasks.put(medicalOfficer.getTask(), medicalOfficer);
        allTasks.put(astroOfficer.getTask(), astroOfficer);
        allTasks.put(moraleOfficer.getTask(), moraleOfficer);
        allTasks.put(nurse.getTask(), nurse);
        allTasks.put(counselor.getTask(), counselor);
    }

    /**
     * Create all persons.
     */
    private void createPersons() throws Exception {
        //<editor-fold defaultstate="collapsed" desc="NX01-Personal">
        jArcher = new UserData();
        jArcher.setFirstName("Jonathan");
        jArcher.setLastName("Archer");
        jArcher.setDistinguishedName("SA-022-9237-CY");
        jArcher.setValidFrom(df.parse("01.01.2112"));
        tPol = new UserData();
        tPol.setFirstName("T");
        tPol.setLastName("Pol");
        tPol.setDistinguishedName("SA-022-1234-VZ");
        tPol.setValidFrom(df.parse("01.01.2088"));
        cTucker = new UserData();
        cTucker.setFirstName("Charles");
        cTucker.setLastName("Tucker");
        cTucker.setDistinguishedName("SA-024-1341-ER");
        cTucker.setValidFrom(df.parse("01.01.2121"));
        cTucker.setValidUntil(df.parse("01.01.2161"));
        mReed = new UserData();
        mReed.setFirstName("Malcom");
        mReed.setLastName("Reed");
        mReed.setDistinguishedName("SA-457-5346-ZT");
        mReed.setValidFrom(df.parse("02.09.2100"));
        hSato = new UserData();
        hSato.setFirstName("Hoshi");
        hSato.setLastName("Sato");
        hSato.setDistinguishedName("SA-897-5480-DR");
        hSato.setValidFrom(df.parse("09.07.2129"));
        tMayweather = new UserData();
        tMayweather.setFirstName("Travis");
        tMayweather.setLastName("Mayweather");
        tMayweather.setDistinguishedName("SA-133-6654-UZ");
        tMayweather.setValidFrom(df.parse("01.01.2126"));
        tMayweather.setValidUntil(df.parse("01.01.2154"));
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="NCC-1701 Personal">
        jKirk = new UserData();
        jKirk.setFirstName("James T.");
        jKirk.setLastName("Kirk");
        jKirk.setDistinguishedName("SC 937-0176 CEC");
        jKirk.setValidFrom(df.parse("22.03.2233"));
        jKirk.setValidUntil(df.parse("01.01.2371"));
        spock = new UserData();
        spock.setFirstName("Spock");
        spock.setLastName("");
        spock.setDistinguishedName("S 179-276 SP");
        spock.setValidFrom(df.parse("01.01.2230"));
        mScott = new UserData();
        mScott.setFirstName("Montgomery");
        mScott.setLastName("Scott");
        mScott.setDistinguishedName("SE 19754 T");
        mScott.setValidFrom(df.parse("01.01.2222"));
        mScott.setValidUntil(df.parse("01.01.2267"));
        lMcCoy = new UserData();
        lMcCoy.setFirstName("Leonard");
        lMcCoy.setLastName("McCoy");
        lMcCoy.setDistinguishedName("DF 53451 G");
        lMcCoy.setValidFrom(df.parse("01.01.2227"));
        lMcCoy.setValidUntil(df.parse("01.01.2364"));
        hSulu = new UserData();
        hSulu.setFirstName("Hikaru");
        hSulu.setLastName("Sulu");
        hSulu.setDistinguishedName("JK 78563 U");
        hSulu.setValidFrom(df.parse("01.01.2237"));
        nUhura = new UserData();
        nUhura.setFirstName("Nyota");
        nUhura.setLastName("Uhura");
        nUhura.setDistinguishedName("HG 21112 C");
        nUhura.setValidFrom(df.parse("01.01.2230"));
        pChekov = new UserData();
        pChekov.setFirstName("Pavel");
        pChekov.setLastName("Chekov");
        pChekov.setDistinguishedName("656-5827B");
        pChekov.setValidFrom(df.parse("01.01.2245"));
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="NCC-1701-D- Personal">
        jlPicard = new UserData();
        jlPicard.setFirstName("Jean-Luc");
        jlPicard.setLastName("Picard");
        jlPicard.setDistinguishedName("SP-937-215");
        jlPicard.setValidFrom(df.parse("13.07.2305"));
        wRiker = new UserData();
        wRiker.setFirstName("William T.");
        wRiker.setLastName("Riker");
        wRiker.setDistinguishedName("SC-231-427");
        wRiker.setValidFrom(df.parse("01.01.2335"));
        data = new UserData();
        data.setFirstName("Data");
        data.setLastName("");
        data.setDistinguishedName("TR-226-299");
        data.setValidFrom(df.parse("02.02.2338"));
        data.setValidUntil(df.parse("01.01.2379"));
        worf = new UserData();
        worf.setFirstName("Worf");
        worf.setLastName("");
        worf.setDistinguishedName("EE-123-089");
        worf.setValidFrom(df.parse("01.01.2340"));
        nYar = new UserData();
        nYar.setFirstName("Natasha");
        nYar.setLastName("Yar");
        nYar.setDistinguishedName("AB-451-892");
        nYar.setValidFrom(df.parse("01.01.2337"));
        nYar.setValidFrom(df.parse("01.01.2364"));
        bCrusher = new UserData();
        bCrusher.setFirstName("Beverly");
        bCrusher.setLastName("Crusher");
        bCrusher.setDistinguishedName("UZ-672-009");
        bCrusher.setValidFrom(df.parse("13.10.2324"));
        dTroy = new UserData();
        dTroy.setFirstName("Deanna");
        dTroy.setLastName("Troy");
        dTroy.setDistinguishedName("WE-125-909");
        dTroy.setValidFrom(df.parse("29.03.2336"));
        mOBrian = new UserData();
        mOBrian.setFirstName("Miles");
        mOBrian.setLastName("O'Brian");
        mOBrian.setDistinguishedName("EB-112-441");
        mOBrian.setValidFrom(df.parse("01.09.2328"));
        mOBrian.setValidUntil(df.parse("01.01.2371"));
        gLaForge = new UserData();
        gLaForge.setFirstName("Geordi");
        gLaForge.setLastName("La Forge");
        gLaForge.setDistinguishedName("OI-981-671");
        gLaForge.setValidFrom(df.parse("16.02.2335"));

//</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="NCC-74656-Personel">
        kJaneway = new UserData();
        kJaneway.setFirstName("Kathryn");
        kJaneway.setLastName("Janeway");
        kJaneway.setDistinguishedName("KJ 134-2367 A");
        kJaneway.setValidFrom(df.parse("20.05.2335"));
        chakotay = new UserData();
        chakotay.setFirstName("Chakotay");
        chakotay.setLastName("");
        chakotay.setDistinguishedName("47-alpha-612");
        chakotay.setValidFrom(df.parse("01.01.2329"));
        tuvok = new UserData();
        tuvok.setFirstName("Tuvok");
        tuvok.setLastName("");
        tuvok.setDistinguishedName("65-beta-123");
        tuvok.setValidFrom(df.parse("01.01.2264"));
        tParis = new UserData();
        tParis.setFirstName("Tom");
        tParis.setLastName("Paris");
        tParis.setDistinguishedName("23-gamma-541");
        bTorres = new UserData();
        bTorres.setFirstName("B'Elanna");
        bTorres.setLastName("Torres");
        bTorres.setDistinguishedName("43-beta-123");
        bTorres.setValidFrom(df.parse("01.01.2349"));
        hKim = new UserData();
        hKim.setFirstName("Harry");
        hKim.setLastName("Kim");
        hKim.setDistinguishedName("HK 523-1245 G");
        hKim.setValidFrom(df.parse("01.01.2349"));
        hKim.setValidUntil(df.parse("01.01.2379"));
        tDoctor = new UserData();
        tDoctor.setFirstName("The");
        tDoctor.setLastName("Doctor");
        tDoctor.setDistinguishedName("TD 001-0100 H");
        tDoctor.setValidFrom(df.parse("01.01.2371"));
        soNine = new UserData();
        soNine.setFirstName("Seven of");
        soNine.setLastName("Nine");
        soNine.setDistinguishedName("SN 007-0090 B");
        soNine.setValidFrom(df.parse("01.01.2350"));
        neelix = new UserData();
        neelix.setFirstName("Neelix");
        neelix.setLastName("");
        neelix.setDistinguishedName("N 167-4236 V");
        neelix.setValidUntil(df.parse("01.01.2374"));
        kes = new UserData();
        kes.setFirstName("Kes");
        kes.setLastName("");
        kes.setDistinguishedName("K 189-2502 O");
        kes.setValidUntil(df.parse("01.01.2369"));
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Misc Persons">
        duras = new UserData();
        duras.setFirstName("Duras");
        duras.setLastName(", son of Ja'rod");
        duras.setDistinguishedName("DKE 41240 0053");
        duras.setValidUntil(df.parse("01.01.2369"));
        surak = new UserData();
        surak.setFirstName("Surak");
        surak.setLastName("");
        surak.setDistinguishedName("SV 5363 11234 A");
        surak.setValidUntil(df.parse("01.01.2269"));
        lTroi = new UserData();
        lTroi.setFirstName("Lwaxana");
        lTroi.setLastName("Troi");
        lTroi.setDistinguishedName("LTB 3001 123");
        borgQueen = new UserData();
        borgQueen.setFirstName("Borg");
        borgQueen.setLastName("Queen");
        borgQueen.setDistinguishedName("BQ 4711");
        //</editor-fold>

        allUsers = new HashMap<>();
        allUsers.put(jArcher.getDistinguishedName(), jArcher);
        allUsers.put(tPol.getDistinguishedName(), tPol);
        allUsers.put(cTucker.getDistinguishedName(), cTucker);
        allUsers.put(mReed.getDistinguishedName(), mReed);
        allUsers.put(hSato.getDistinguishedName(), hSato);
        allUsers.put(tMayweather.getDistinguishedName(), tMayweather);
        allUsers.put(jKirk.getDistinguishedName(), jKirk);
        allUsers.put(spock.getDistinguishedName(), spock);
        allUsers.put(mScott.getDistinguishedName(), mScott);
        allUsers.put(lMcCoy.getDistinguishedName(), lMcCoy);
        allUsers.put(hSulu.getDistinguishedName(), hSulu);
        allUsers.put(nUhura.getDistinguishedName(), nUhura);
        allUsers.put(pChekov.getDistinguishedName(), pChekov);
        allUsers.put(jlPicard.getDistinguishedName(), jlPicard);
        allUsers.put(wRiker.getDistinguishedName(), wRiker);
        allUsers.put(data.getDistinguishedName(), data);
        allUsers.put(worf.getDistinguishedName(), worf);
        allUsers.put(nYar.getDistinguishedName(), nYar);
        allUsers.put(bCrusher.getDistinguishedName(), bCrusher);
        allUsers.put(dTroy.getDistinguishedName(), dTroy);
        allUsers.put(mOBrian.getDistinguishedName(), mOBrian);
        allUsers.put(gLaForge.getDistinguishedName(), gLaForge);
        allUsers.put(kJaneway.getDistinguishedName(), kJaneway);
        allUsers.put(chakotay.getDistinguishedName(), chakotay);
        allUsers.put(tuvok.getDistinguishedName(), tuvok);
        allUsers.put(tParis.getDistinguishedName(), tParis);
        allUsers.put(bTorres.getDistinguishedName(), bTorres);
        allUsers.put(hKim.getDistinguishedName(), hKim);
        allUsers.put(tDoctor.getDistinguishedName(), tDoctor);
        allUsers.put(soNine.getDistinguishedName(), soNine);
        allUsers.put(neelix.getDistinguishedName(), neelix);
        allUsers.put(kes.getDistinguishedName(), kes);
        allUsers.put(duras.getDistinguishedName(), duras);
        allUsers.put(surak.getDistinguishedName(), surak);
        allUsers.put(lTroi.getDistinguishedName(), lTroi);
        allUsers.put(borgQueen.getDistinguishedName(), borgQueen);
    }

    /**
     * Create all OrganizationUnits.
     */
    private void createOrganizationUnits() {
        uFederation = EntityCreationHelper.createOrganizationUnit("United Federation of Planets", allUsers.get(jArcher.getDistinguishedName()), "Paris", "Place de la Concorde", "75001", "France, Earth", "http://en.memory-alpha.org/wiki/United_Federation_of_Planets");
        klingons = EntityCreationHelper.createOrganizationUnit("Klingon Empire", allUsers.get(duras.getDistinguishedName()), "First City", null, null, "Qo'noS", "http://en.memory-alpha.org/wiki/Klingon_Empire");
        vulcan = EntityCreationHelper.createOrganizationUnit("Vulcanians", allUsers.get(surak.getDistinguishedName()), null, null, null, "Vulcan", "http://en.memory-alpha.org/wiki/Vulcan");
        betazoid = EntityCreationHelper.createOrganizationUnit("Betazoid", allUsers.get(lTroi.getDistinguishedName()), null, null, null, "Betazed", "http://en.memory-alpha.org/wiki/Betazoid");
        borg = EntityCreationHelper.createOrganizationUnit("Borg", allUsers.get(borgQueen.getDistinguishedName()), null, null, null, "Unicomplex", "http://en.memory-alpha.org/wiki/Borg");
        talaxians = EntityCreationHelper.createOrganizationUnit("Talaxian", null, "Paxau", null, null, "Talax", "http://en.memory-alpha.org/wiki/Talaxian");
        ocampa = EntityCreationHelper.createOrganizationUnit("Ocampa", null, "Underground City", null, null, "Ocampa", "http://en.memory-alpha.org/wiki/Ocampa");

        allOrganizationUnits = new HashMap<>();
        allOrganizationUnits.put(uFederation.getOuName(), uFederation);
        allOrganizationUnits.put(klingons.getOuName(), klingons);
        allOrganizationUnits.put(vulcan.getOuName(), vulcan);
        allOrganizationUnits.put(betazoid.getOuName(), betazoid);
        allOrganizationUnits.put(borg.getOuName(), borg);
        allOrganizationUnits.put(talaxians.getOuName(), talaxians);
        allOrganizationUnits.put(ocampa.getOuName(), ocampa);
    }

    /**
     * Create all studies.
     */
    private void createStudies() throws Exception {
        nx1 = EntityCreationHelper.createStudy("NX-01",
                allUsers.get(jArcher.getDistinguishedName()),
                new OrganizationUnit[]{allOrganizationUnits.get(uFederation.getOuName()), allOrganizationUnits.get(klingons.getOuName())},
                df.parse("16.04.2151"),
                df.parse("01.01.2370"));
        ncc1701 = EntityCreationHelper.createStudy("NCC-1701",
                allUsers.get(jKirk.getDistinguishedName()),
                new OrganizationUnit[]{allOrganizationUnits.get(uFederation.getOuName()), allOrganizationUnits.get(vulcan.getOuName())},
                df.parse("01.01.2265"),
                df.parse("01.01.2269"));
        ncc1701d = EntityCreationHelper.createStudy("NCC-1701-D",
                allUsers.get(jlPicard.getDistinguishedName()),
                new OrganizationUnit[]{allOrganizationUnits.get(uFederation.getOuName()), allOrganizationUnits.get(klingons.getOuName()), allOrganizationUnits.get(betazoid.getOuName())},
                df.parse("01.01.2364"),
                df.parse("01.01.2370"));
        ncc74656 = EntityCreationHelper.createStudy("NCC-74656",
                allUsers.get(kJaneway.getDistinguishedName()),
                new OrganizationUnit[]{allOrganizationUnits.get(uFederation.getOuName()), allOrganizationUnits.get(vulcan.getOuName()), allOrganizationUnits.get(klingons.getOuName()), allOrganizationUnits.get(borg.getOuName()), allOrganizationUnits.get(talaxians.getOuName()), allOrganizationUnits.get(ocampa.getOuName())},
                df.parse("01.01.2371"),
                df.parse("01.01.2378"));

        allStudies = new HashMap<>();
        allStudies.put(nx1.getTopic(), nx1);
        allStudies.put(ncc1701.getTopic(), ncc1701);
        allStudies.put(ncc1701d.getTopic(), ncc1701d);
        allStudies.put(ncc74656.getTopic(), ncc74656);
    }

    //<editor-fold defaultstate="collapsed" desc="Create Investigations (5 per study including personel, each with 2 metadata schemas)">
    private void createInvestigations() throws Exception {
        createNX01Investigations();
        createNCC1701Investigations();
        createNCC1701DInvestigations();
        createNCC74656Investigations();
        allNX1Investigations = new HashMap<>();
        allNCC1701Investigations = new HashMap<>();
        allNCC74656Investigations = new HashMap<>();
        allNCC1701DInvestigations = new HashMap<>();

        allNX1Investigations.put(nx1_1.getTopic(), nx1_1);
        allNX1Investigations.put(nx1_2.getTopic(), nx1_2);
        allNX1Investigations.put(nx1_3.getTopic(), nx1_3);
        allNX1Investigations.put(nx1_4.getTopic(), nx1_4);
        allNX1Investigations.put(nx1_5.getTopic(), nx1_5);

        allNCC1701Investigations.put(ncc1701_1.getTopic(), ncc1701_1);
        allNCC1701Investigations.put(ncc1701_2.getTopic(), ncc1701_2);
        allNCC1701Investigations.put(ncc1701_3.getTopic(), ncc1701_3);
        allNCC1701Investigations.put(ncc1701_4.getTopic(), ncc1701_4);
        allNCC1701Investigations.put(ncc1701_5.getTopic(), ncc1701_5);

        allNCC1701DInvestigations.put(ncc1701d_1.getTopic(), ncc1701d_1);
        allNCC1701DInvestigations.put(ncc1701d_2.getTopic(), ncc1701d_2);
        allNCC1701DInvestigations.put(ncc1701d_3.getTopic(), ncc1701d_3);
        allNCC1701DInvestigations.put(ncc1701d_4.getTopic(), ncc1701d_4);
        allNCC1701DInvestigations.put(ncc1701d_5.getTopic(), ncc1701d_5);

        allNCC74656Investigations.put(ncc74656_1.getTopic(), ncc74656_1);
        allNCC74656Investigations.put(ncc74656_2.getTopic(), ncc74656_2);
        allNCC74656Investigations.put(ncc74656_3.getTopic(), ncc74656_3);
        allNCC74656Investigations.put(ncc74656_4.getTopic(), ncc74656_4);
        allNCC74656Investigations.put(ncc74656_5.getTopic(), ncc74656_5);
    }

    private void createNX01Investigations() throws Exception {
        nx1_1 = Investigation.factoryNewInvestigation();
        nx1_1.setTopic("Broken Bow");
        nx1_1.setDescription("Earth launches its first starship of exploration, Enterprise, on a mission to return an injured Klingon to his homeworld.");
        nx1_1.setStartDate(df.parse("16.04.2151"));
        assignNX01Personal(nx1_1);
        assignMetadataSchemas(nx1_1);
        nx1_2 = Investigation.factoryNewInvestigation();
        nx1_2.setTopic("Flight or Flight");
        nx1_2.setDescription("Enterprise finds an abandoned ship, filled with corpses which appear to have been used for an experiment.");
        nx1_2.setStartDate(df.parse("06.05.2151"));
        assignNX01Personal(nx1_2);
        assignMetadataSchemas(nx1_2);
        nx1_3 = Investigation.factoryNewInvestigation();
        nx1_3.setTopic("Strange New World");
        nx1_3.setDescription("An Enterprise landing party believes T'Pol is conspiring with a species of rock creatures on a strange class M planet.");
        assignNX01Personal(nx1_3);
        assignMetadataSchemas(nx1_3);
        nx1_4 = Investigation.factoryNewInvestigation();
        nx1_4.setTopic("Unexpected");
        nx1_4.setDescription("When Trip Tucker assists an alien vessel with repairs, a \"friendly\" encounter with one of the crew leads to rather unexpected consequences. ");
        assignNX01Personal(nx1_4);
        assignMetadataSchemas(nx1_4);
        nx1_5 = Investigation.factoryNewInvestigation();
        nx1_5.setTopic("Terra Nova");
        nx1_5.setDescription("Enterprise investigates the mystery of a lost Earth colony whose inhabitants disappeared decades before. But that doesn't mean they left.");
        assignNX01Personal(nx1_5);
        assignMetadataSchemas(nx1_5);

        nx1.addInvestigation(nx1_1);
        nx1.addInvestigation(nx1_2);
        nx1.addInvestigation(nx1_3);
        nx1.addInvestigation(nx1_4);
        nx1.addInvestigation(nx1_5);
    }

    private void assignNX01Personal(Investigation investigation) {
        investigation.addParticipant(new Participant(allUsers.get(jArcher.getDistinguishedName()), allTasks.get(captain.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(tPol.getDistinguishedName()), allTasks.get(scienceOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(cTucker.getDistinguishedName()), allTasks.get(chiefEngineer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(mReed.getDistinguishedName()), allTasks.get(tacticalOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(hSato.getDistinguishedName()), allTasks.get(comOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(tMayweather.getDistinguishedName()), allTasks.get(helmsman.getTask())));
    }

    private void createNCC1701Investigations() throws Exception {
        ncc1701_1 = Investigation.factoryNewInvestigation();
        ncc1701_1.setTopic("Where No Man Has Gone Before");
        ncc1701_1.setStartDate(df.parse("01.01.2265"));
        ncc1701_1.setDescription("An encounter at the limits of our galaxy begins to change Lieutenant Commander Gary Mitchell and threatens the future of the Enterprise and the Human race itself.");
        assignNCC1701Personal(ncc1701_1);
        assignMetadataSchemas(ncc1701_1);

        ncc1701_2 = Investigation.factoryNewInvestigation();
        ncc1701_2.setTopic("The Corbomite Maneuver");
        ncc1701_2.setStartDate(df.parse("01.01.2266"));
        ncc1701_2.setDescription("Exploring a distant region of space, the Enterprise is threatened by Balok, commander of a starship from the First Federation.");
        assignNCC1701Personal(ncc1701_2);
        assignMetadataSchemas(ncc1701_2);

        ncc1701_3 = Investigation.factoryNewInvestigation();
        ncc1701_3.setTopic("Mudd's Women");
        ncc1701_3.setStartDate(df.parse("01.01.2266"));
        ncc1701_3.setDescription("The Enterprise rescues a con man named Harry Mudd who is trafficking in mail-order brides. ");
        assignNCC1701Personal(ncc1701_3);
        assignMetadataSchemas(ncc1701_3);

        ncc1701_4 = Investigation.factoryNewInvestigation();
        ncc1701_4.setTopic("The Enemy Within");
        ncc1701_4.setStartDate(df.parse("01.01.2266"));
        ncc1701_4.setDescription("A transporter malfunction splits Captain Kirk into two people Ã¢â‚¬â€œ one good and one evil, and neither capable of functioning well separately.");
        assignNCC1701Personal(ncc1701_4);
        assignMetadataSchemas(ncc1701_4);

        ncc1701_5 = Investigation.factoryNewInvestigation();
        ncc1701_5.setTopic("The Man Trap");
        ncc1701_5.setStartDate(df.parse("01.01.2266"));
        ncc1701_5.setDescription("A mysterious creature stalks the Enterprise, murdering crew members.");
        assignNCC1701Personal(ncc1701_5);
        assignMetadataSchemas(ncc1701_5);

        ncc1701.addInvestigation(ncc1701_1);
        ncc1701.addInvestigation(ncc1701_2);
        ncc1701.addInvestigation(ncc1701_3);
        ncc1701.addInvestigation(ncc1701_4);
        ncc1701.addInvestigation(ncc1701_5);
    }

    private void assignNCC1701Personal(Investigation investigation) {
        investigation.addParticipant(new Participant(allUsers.get(jKirk.getDistinguishedName()), allTasks.get(captain.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(spock.getDistinguishedName()), allTasks.get(scienceOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(mScott.getDistinguishedName()), allTasks.get(chiefEngineer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(lMcCoy.getDistinguishedName()), allTasks.get(medicalOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(hSulu.getDistinguishedName()), allTasks.get(helmsman.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(nUhura.getDistinguishedName()), allTasks.get(comOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(pChekov.getDistinguishedName()), allTasks.get(navigator.getTask())));
    }

    private void createNCC1701DInvestigations() throws Exception {
        ncc1701d_1 = Investigation.factoryNewInvestigation();
        ncc1701d_1.setTopic("Encounter at Farpoint");
        ncc1701d_1.setDescription("Captain Jean-Luc Picard leads the crew of the USS Enterprise-D on its maiden voyage, "
                + " examine a new planetary station for trade with the Federation. On the way, they encounter Q, "
                + "an omnipotent extra-dimensional being, who challenges humanity as a barbaric, inferior species. "
                + "Picard and his new crew must hold off Q's challenge and solve the puzzle of Farpoint station on "
                + "Deneb IV, a base that is far more than it seems to be.");
        ncc1701d_1.setStartDate(df.parse("01.01.2364"));
        assignNCC1701DPersonal(ncc1701d_1);
        assignMetadataSchemas(ncc1701d_1);

        ncc1701d_2 = Investigation.factoryNewInvestigation();
        ncc1701d_2.setTopic("The Naked Now");
        ncc1701d_2.setDescription("The crew of the Enterprise is subjected to an exotic illness that drives them to unusual manic behavior.");
        ncc1701d_2.setStartDate(df.parse("01.01.2364"));
        assignNCC1701DPersonal(ncc1701d_2);
        assignMetadataSchemas(ncc1701d_2);

        ncc1701d_3 = Investigation.factoryNewInvestigation();
        ncc1701d_3.setTopic("Code of Honor");
        ncc1701d_3.setDescription("A mission of mercy is jeopardized when a planetary ruler decides he wants an Enterprise officer as his wife.");
        ncc1701d_3.setStartDate(df.parse("01.01.2364"));
        assignNCC1701DPersonal(ncc1701d_3);
        assignMetadataSchemas(ncc1701d_3);

        ncc1701d_4 = Investigation.factoryNewInvestigation();
        ncc1701d_4.setTopic("The Last Outpost");
        ncc1701d_4.setDescription("In pursuit of Ferengi marauders, the Enterprise and its quarry become trapped by a mysterious planet that is draining both ships' energies.");
        ncc1701d_4.setStartDate(df.parse("01.01.2364"));
        assignNCC1701DPersonal(ncc1701d_4);
        assignMetadataSchemas(ncc1701d_4);

        ncc1701d_5 = Investigation.factoryNewInvestigation();
        ncc1701d_5.setTopic("Where No One Has Gone Before");
        ncc1701d_5.setDescription("When an experimental engine modification throws the Enterprise to the edge of the known universe, the crew must rely on a mysterious alien to guide the ship home.");
        ncc1701d_5.setStartDate(df.parse("01.01.2364"));
        assignNCC1701DPersonal(ncc1701d_5);
        assignMetadataSchemas(ncc1701d_5);

        ncc1701d.addInvestigation(ncc1701d_1);
        ncc1701d.addInvestigation(ncc1701d_2);
        ncc1701d.addInvestigation(ncc1701d_3);
        ncc1701d.addInvestigation(ncc1701d_4);
        ncc1701d.addInvestigation(ncc1701d_5);
    }

    private void assignNCC1701DPersonal(Investigation investigation) {
        investigation.addParticipant(new Participant(allUsers.get(jlPicard.getDistinguishedName()), allTasks.get(captain.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(wRiker.getDistinguishedName()), allTasks.get(firstOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(data.getDistinguishedName()), allTasks.get(scienceOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(gLaForge.getDistinguishedName()), allTasks.get(chiefEngineer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(nYar.getDistinguishedName()), allTasks.get(tacticalOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(worf.getDistinguishedName()), allTasks.get(tacticalOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(bCrusher.getDistinguishedName()), allTasks.get(medicalOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(dTroy.getDistinguishedName()), allTasks.get(counselor.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(mOBrian.getDistinguishedName()), allTasks.get(flightController.getTask())));
    }

    private void createNCC74656Investigations() throws Exception {
        ncc74656_1 = Investigation.factoryNewInvestigation();
        ncc74656_1.setTopic("Caretaker");
        ncc74656_1.setDescription("The newly commissioned starship Voyager and a Maquis raider are flung into the remote Delta Quadrant by a powerful entity known as the Caretaker.");
        ncc74656_1.setStartDate(df.parse("01.01.2371"));
        assignNCC74656Personal(ncc74656_1);
        assignMetadataSchemas(ncc74656_1);

        ncc74656_2 = Investigation.factoryNewInvestigation();
        ncc74656_2.setTopic("Parallax");
        ncc74656_2.setDescription("Investigating an apparent distress call, Voyager becomes trapped inside the event horizon of a quantum singularity.");
        ncc74656_2.setStartDate(df.parse("01.01.2371"));
        assignNCC74656Personal(ncc74656_2);
        assignMetadataSchemas(ncc74656_2);

        ncc74656_3 = Investigation.factoryNewInvestigation();
        ncc74656_3.setTopic("Time and Again");
        ncc74656_3.setDescription("While investigating a massive explosion that destroyed all life on a planet, Janeway and Paris are swept back a day in time, where they must prevent the explosion.");
        ncc74656_3.setStartDate(df.parse("01.01.2371"));
        assignNCC74656Personal(ncc74656_3);
        assignMetadataSchemas(ncc74656_3);

        ncc74656_4 = Investigation.factoryNewInvestigation();
        ncc74656_4.setTopic("Phage");
        ncc74656_4.setDescription("Neelix's lungs are removed by a race that suffers from a deadly phage that is slowly destroying their population prompting them to harvest replacement organs and tissues from other species.");
        ncc74656_4.setStartDate(df.parse("01.01.2371"));
        assignNCC74656Personal(ncc74656_4);
        assignMetadataSchemas(ncc74656_4);

        ncc74656_5 = Investigation.factoryNewInvestigation();
        ncc74656_5.setTopic("The Cloud");
        ncc74656_5.setDescription("With energy reserves nearly depleted, Voyager investigates possible resources inside a nebula, which - as it turns out - is not really a nebula.");
        ncc74656_5.setStartDate(df.parse("01.01.2371"));
        assignNCC74656Personal(ncc74656_5);
        assignMetadataSchemas(ncc74656_5);

        ncc74656.addInvestigation(ncc74656_1);
        ncc74656.addInvestigation(ncc74656_2);
        ncc74656.addInvestigation(ncc74656_3);
        ncc74656.addInvestigation(ncc74656_4);
        ncc74656.addInvestigation(ncc74656_5);
    }

    private void assignNCC74656Personal(Investigation investigation) {
        investigation.addParticipant(new Participant(allUsers.get(kJaneway.getDistinguishedName()), allTasks.get(captain.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(chakotay.getDistinguishedName()), allTasks.get(firstOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(tuvok.getDistinguishedName()), allTasks.get(tacticalOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(tParis.getDistinguishedName()), allTasks.get(helmsman.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(bTorres.getDistinguishedName()), allTasks.get(chiefEngineer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(hKim.getDistinguishedName()), allTasks.get(operationsOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(tDoctor.getDistinguishedName()), allTasks.get(medicalOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(soNine.getDistinguishedName()), allTasks.get(astroOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(neelix.getDistinguishedName()), allTasks.get(moraleOfficer.getTask())));
        investigation.addParticipant(new Participant(allUsers.get(kes.getDistinguishedName()), allTasks.get(nurse.getTask())));
    }

    private void assignMetadataSchemas(Investigation investigation) {
        investigation.addMetaDataSchema(allSchemas.get(dc.getSchemaIdentifier()));
        investigation.addMetaDataSchema(allSchemas.get(bmd.getSchemaIdentifier()));
    }
//</editor-fold>

    /**
     * Store the entire metadata structure via REST interfaces into the
     * database.
     *
     * @param baseUrl The REST base URL, e.g. "http://localhost:8080/KITDM"
     * @param pContext The REST context.
     *
     * @throws Exception if anything goes wrong.
     */
    public void storeMetaData(String baseUrl, SimpleRESTContext pContext) throws Exception {
        String groupId = Constants.USERS_GROUP_ID;
        UserGroupRestClient userGroupClient = new UserGroupRestClient(baseUrl + "/rest/usergroup/", pContext);

        //create persons
        createPersons();
        //save all persons
        Set<Entry<String, UserData>> keys = allUsers.entrySet();
        for (Entry<String, UserData> entry : keys) {
            LOGGER.debug("Adding user " + entry.getKey());
            if (entry.getKey() == null) {
                LOGGER.warn("Invalid distinguished name! Skipping user " + entry.getKey());
            } else {
                LOGGER.debug("Adding user {}", entry.getKey());
                UserData result = userGroupClient.addUser(groupId,
                        (entry.getValue().getFirstName() != null) ? entry.getValue().getFirstName() : "",
                        (entry.getValue().getLastName() != null) ? entry.getValue().getLastName() : "",
                        (entry.getValue().getEmail() != null) ? entry.getValue().getEmail() : "",
                        entry.getValue().getDistinguishedName()).getEntities().get(0);
                allUsers.put(entry.getKey(), result);
            }
        }

        //create tasks
        createTasks();
        //save all tasks
        Set<Entry<String, Task>> taskKeys = allTasks.entrySet();
        BaseMetaDataRestClient baseMetadataClient = new BaseMetaDataRestClient(baseUrl + "/rest/basemetadata/", pContext);
        for (Entry<String, Task> entry : taskKeys) {
            LOGGER.debug("Adding task " + entry.getKey());
            Task result = baseMetadataClient.addTask(entry.getValue(), groupId).getEntities().get(0);
            allTasks.put(entry.getKey(), result);
        }

        //create metadata schemas
        createMetadataSchemas();
        //save all metadata schemas
        Set<Entry<String, MetaDataSchema>> schemaKeys = allSchemas.entrySet();
        for (Entry<String, MetaDataSchema> entry : schemaKeys) {
            LOGGER.debug("Adding metadata schemas " + entry.getKey());
            MetaDataSchema result = baseMetadataClient.addMetadataSchema(entry.getValue(), groupId).getEntities().get(0);
            allSchemas.put(entry.getKey(), result);
        }

        //create OUs with managers from saved persons
        createOrganizationUnits();
        //save OUs
        Set<Entry<String, OrganizationUnit>> ouKeys = allOrganizationUnits.entrySet();
        for (Entry<String, OrganizationUnit> entry : ouKeys) {
            LOGGER.debug("Adding OrganizationUnit " + entry.getKey());
            OrganizationUnit result = baseMetadataClient.addOrganizationUnit(entry.getValue(), groupId).getEntities().get(0);
            allOrganizationUnits.put(entry.getKey(), result);
        }

        //create studies with saved OUs
        createStudies();
        //save studies
        Set<Entry<String, Study>> studyKeys = allStudies.entrySet();
        for (Entry<String, Study> entry : studyKeys) {
            LOGGER.debug("Adding Study " + entry.getKey());
            Set<Relation> relations = entry.getValue().getOrganizationUnits();
            Study result = baseMetadataClient.addStudy(entry.getValue(), groupId).getEntities().get(0);
            LOGGER.debug("Adding relations to study.");
            for (Relation relation : relations) {
                result = baseMetadataClient.addRelationToStudy(result.getStudyId(), relation, groupId).getEntities().get(0);
            }
            LOGGER.debug("Adding stored study to study map.");
            allStudies.put(entry.getKey(), result);
        }

        //create investigations with saved studies
        createInvestigations();
        //save investigations and associated files
        storeInvestigations(allNX1Investigations, nx1, baseMetadataClient, groupId);
        storeInvestigations(allNCC1701Investigations, ncc1701, baseMetadataClient, groupId);
        storeInvestigations(allNCC1701DInvestigations, ncc1701d, baseMetadataClient, groupId);
        storeInvestigations(allNCC74656Investigations, ncc74656, baseMetadataClient, groupId);
    }

    /**
     * Store a map of investigations. This call was outsourced from {@link #storeMetaData(java.lang.String, edu.kit.lsdf.kdm.rest.SimpleRESTContext)
     * } as it contains a lot of redundancy for the investigations belonging to
     * single studies.
     *
     * @param investigations The map of investigations to store.
     * @param pStudy The study the investigation will be added to.
     * @param pClient The REST client used to store the investigations.
     * @param pGroupId The groupId used to store the investigations.
     */
    private void storeInvestigations(Map<String, Investigation> investigations, Study pStudy, BaseMetaDataRestClient pClient, String pGroupId) {
        Set<Entry<String, Investigation>> investigationKeys = investigations.entrySet();
        for (Entry<String, Investigation> entry : investigationKeys) {
            Set<Participant> participants = entry.getValue().getParticipants();
            Set<MetaDataSchema> schemas = entry.getValue().getMetaDataSchema();
            LOGGER.debug("Adding Investigation " + entry.getKey());
            Investigation result = pClient.addInvestigationToStudy(allStudies.get(pStudy.getTopic()).getStudyId(), entry.getValue(), pGroupId).getEntities().get(0);
            LOGGER.debug("Adding participants to investigation.");
            for (Participant participant : participants) {
                result = pClient.addParticipantToInvestigation(result.getInvestigationId(), participant, pGroupId).getEntities().get(0);
            }
            LOGGER.debug("Adding metadata schemas to investigation.");
            for (MetaDataSchema schema : schemas) {
                result = pClient.addMetadataSchemaToInvestigation(result.getInvestigationId(), schema, pGroupId).getEntities().get(0);
            }
            LOGGER.debug("Adding stored investigation to investigation map.");
            investigations.put(entry.getKey(), result);
        }
    }

    /**
     * Load the entire metadata structure from the database. While loading the
     * expected amount of entries per type is checked. If there are less
     * entries, an exception is thrown.
     *
     * @param baseUrl The REST base URL, e.g. "http://localhost:8080/KITDM"
     * @param pContext The REST context.
     *
     * @throws Exception If anything goes wrong, e.g. if the amount of any
     * entity is smaller than the expected amount.
     */
    public void loadMetaData(String baseUrl, SimpleRESTContext pContext) throws Exception {
        String groupId = Constants.USERS_GROUP_ID;
        UserGroupRestClient userGroupClient = new UserGroupRestClient(baseUrl + "/rest/usergroup/", pContext);
        createPersons();
        List<UserData> users = userGroupClient.getAllUsers(groupId, 0, 100).getEntities();
        LOGGER.debug("Loading users");
        int itemCount = 0;
        for (UserData u_tmp : users) {
            UserData u = userGroupClient.getUserById(u_tmp.getUserId()).getWrappedEntities().get(0);
            if (allUsers.containsKey(u.getDistinguishedName())) {
                LOGGER.debug("Loaded user with dn " + u.getDistinguishedName());
                allUsers.put(u.getDistinguishedName(), u);
                itemCount++;
            } else {
                LOGGER.debug(" - Ignoring user with dn " + u.getDistinguishedName());
            }
        }

        if (itemCount != 36) {
            throw new Exception("User count is " + itemCount + " but should be 36");
        }

        BaseMetaDataRestClient baseMetadataClient = new BaseMetaDataRestClient(baseUrl + "/rest/basemetadata/", pContext);
        createTasks();
        List<Task> tasks = baseMetadataClient.getAllTasks(0, 100, groupId).getEntities();
        LOGGER.debug("Loading tasks");
        itemCount = 0;
        for (Task t_tmp : tasks) {
            Task t = baseMetadataClient.getTaskById(t_tmp.getTaskId(), groupId).getWrappedEntities().get(0);
            if (allTasks.containsKey(t.getTask())) {
                LOGGER.debug("Loaded task with name " + t.getTask());
                allTasks.put(t.getTask(), t);
                itemCount++;
            } else {
                LOGGER.debug(" - Ignoring task with name " + t.getTask());
            }
        }

        if (itemCount != 15) {
            throw new Exception("Task count is " + itemCount + " but should be 15");
        }
        createMetadataSchemas();
        List<MetaDataSchema> meta = baseMetadataClient.getAllMetadataSchemas(0, 100, groupId).getEntities();
        LOGGER.debug("Loading MetaDataSchemas");
        itemCount = 0;
        for (MetaDataSchema s_tmp : meta) {
            MetaDataSchema s = baseMetadataClient.getMetadataSchemaById(s_tmp.getId(), groupId).getWrappedEntities().get(0);
            if (allSchemas.containsKey(s.getSchemaIdentifier())) {
                LOGGER.debug("Loaded schema with id " + s.getSchemaIdentifier());
                allSchemas.put(s.getSchemaIdentifier(), s);
                itemCount++;
            } else {
                LOGGER.debug(" - Ignoring schema with id " + s.getSchemaIdentifier());
            }
        }

        if (itemCount != 2) {
            throw new Exception("Schema count is " + itemCount + " but should be 2");
        }

        createOrganizationUnits();
        List<OrganizationUnit> ous = baseMetadataClient.getAllOrganizationUnits(0, 100, groupId).getEntities();
        LOGGER.debug("Loading OrganizationUnits");
        itemCount = 0;
        for (OrganizationUnit o_tmp : ous) {
            OrganizationUnit o = baseMetadataClient.getOrganizationUnitById(o_tmp.getOrganizationUnitId(), groupId).getWrappedEntities().get(0);
            if (o.getManager() != null) {
                if (allOrganizationUnits.containsKey(o.getOuName())) {
                    LOGGER.debug("Loaded ou with name  " + o.getOuName());
                    allOrganizationUnits.put(o.getOuName(), o);
                    itemCount++;
                } else {
                    LOGGER.debug(" - Ignoring ou with name " + o.getOuName());
                }
            } else {
                LOGGER.warn("Ou " + o.getOuName() + " has no manager defined. Ignoring Ou.");
            }
        }
        if (itemCount != 7) {
            throw new Exception("OrganizationUnit count is " + itemCount + " but should be 7");
        }

        createStudies();
        List<Study> studies = baseMetadataClient.getAllStudies(0, 100, groupId).getEntities();
        LOGGER.debug("Loading Studies");
        itemCount = 0;
        for (Study s_tmp : studies) {
            Study s = baseMetadataClient.getStudyById(s_tmp.getStudyId(), groupId).getWrappedEntities().get(0);
            if (allStudies.containsKey(s.getTopic())) {
                LOGGER.debug("Loaded study with topic  " + s.getTopic());
                allStudies.put(s.getTopic(), s);

                LOGGER.debug("Checking relations of study");
                int relationCount = 2; //default for NX-01 and NCC-1701
                if (null != s.getTopic()) {
                    switch (s.getTopic()) {
                        case "NCC-1701-D":
                            relationCount = 3;
                            break;
                        case "NCC-74656":
                            relationCount = 6;
                            break;
                    }
                }

                if (s.getOrganizationUnits().size() != relationCount) {
                    throw new Exception("Study relations count is " + s.getOrganizationUnits().size() + " but should be " + relationCount);
                }

                LOGGER.debug("Checking investigations of study");
                if (s.getInvestigations().size() != 5) {
                    throw new Exception("Expected 5 investigations per study but retrieved " + s.getInvestigations().size());
                }

                if (s.getManager() == null) {
                    throw new Exception("Expected a manager for each study but found none");
                }

                itemCount++;
            } else {
                LOGGER.debug(" - Ignoring study with topic " + s.getTopic());
            }
        }
        if (itemCount != 4) {
            throw new Exception("Studies count is " + itemCount + " but should be 4");
        }

        createInvestigations();
        Set<Entry<String, Study>> studyEntries = allStudies.entrySet();
        itemCount = 0;
        for (Entry<String, Study> entry : studyEntries) {
            String topic = entry.getValue().getTopic();
            //load next block of 5 investigations (5 per study)
            List<Investigation> investigations = baseMetadataClient.getAllInvestigations(entry.getValue().getStudyId(), 0, 100, groupId).getEntities();
            int expectedStaff = 0;
            Map<String, Investigation> invMap = null;
            LOGGER.debug("Loading investigation for study {} with id {}", topic, entry.getValue().getStudyId());
            if (nx1.getTopic().equals(topic)) {
                LOGGER.debug("Handling NX1 investigations");
                loadInvestigations(baseMetadataClient, investigations, allNX1Investigations, groupId);
                itemCount += investigations.size();
                expectedStaff = 6;
                invMap = allNX1Investigations;
            } else if (ncc1701.getTopic().equals(topic)) {
                LOGGER.debug("Handling NCC1701 investigations");
                loadInvestigations(baseMetadataClient, investigations, allNCC1701Investigations, groupId);
                itemCount += investigations.size();
                expectedStaff = 7;
                invMap = allNCC1701Investigations;
            } else if (ncc1701d.getTopic().equals(topic)) {
                LOGGER.debug("Handling NCC1701D investigations");
                loadInvestigations(baseMetadataClient, investigations, allNCC1701DInvestigations, groupId);
                itemCount += investigations.size();
                expectedStaff = 9;
                invMap = allNCC1701DInvestigations;
            } else if (ncc74656.getTopic().equals(topic)) {
                LOGGER.debug("Handling NCC74656 investigations");
                loadInvestigations(baseMetadataClient, investigations, allNCC74656Investigations, groupId);
                itemCount += investigations.size();
                expectedStaff = 10;
                invMap = allNCC74656Investigations;
            } else {
                throw new Exception("Invalid topic '" + topic + "' found.");
            }

            int mdSchemas = 0;
            int staff = 0;
            int objects = 0;

            for (Investigation i : invMap.values()) {
                mdSchemas += i.getMetaDataSchema().size();
                staff += i.getParticipants().size();
                objects += i.getDataSets().size();
            }

            if (objects != 5) {
                throw new Exception("Expecting 5 digital objects, but found " + objects);
            }

            if (mdSchemas != 10) {
                throw new Exception("Expecting 40 metadata schemas (2 investigation), but found " + mdSchemas);
            }

            if (staff != expectedStaff * 5) {//5 investigations, each with the same staff
                throw new Exception("Expecting staff size of " + (expectedStaff * 5) + " but found " + staff);
            }

        }
        if (itemCount != 20) {
            throw new Exception("Investigation count is " + itemCount + " but should be 20.");
        }
    }

    /**
     * Load a map of investigations. This call was outsourced from {@link #loadMetaData(java.lang.String, edu.kit.lsdf.kdm.rest.SimpleRESTContext)
     * } as it contains a lot of redundancy for the investigations belonging to
     * single studies.
     *
     * @param pClient The REST client used to load the investigations.
     * @param investigations The list of investigations to load.
     * @param pTargetMap The target map where to store the loaded
     * investigations.
     * @param pGroupId The groupId used to store the investigations.
     */
    private void loadInvestigations(BaseMetaDataRestClient pClient, List<Investigation> pInvestigations, Map<String, Investigation> pTargetMap, String pGroupId) {
        LOGGER.debug("Loading investigations from map {} " + pInvestigations);
        for (Investigation i_tmp : pInvestigations) {
            LOGGER.debug("Loading investigation with id {}", i_tmp.getInvestigationId());
            Investigation i = pClient.getInvestigationById(i_tmp.getInvestigationId(), pGroupId).getWrappedEntities().get(0);
            if (pTargetMap.containsKey(i.getTopic())) {
                LOGGER.debug("Putting investigation with topic  " + i.getTopic() + " to investigation map.");
                pTargetMap.put(i.getTopic(), i);
            } else {
                LOGGER.debug(" - Ignoring investigation with topic " + i.getTopic());
            }
        }
    }

    /**
     * Ingest the predefined data into the previously created metadata
     * structure.
     *
     * @param pBaseUrl The REST base URL, e.g. "http://localhost:8080/KITDM"
     * @param pContext The REST context.
     *
     * @throws Exception if something goes wrong.
     */
    public void ingestData(String pBaseUrl, SimpleRESTContext pContext) throws Exception {
        buildObjectStructure(pBaseUrl, nx1, allNX1Investigations, pContext);
        buildObjectStructure(pBaseUrl, ncc1701, allNCC1701Investigations, pContext);
        buildObjectStructure(pBaseUrl, ncc1701d, allNCC1701DInvestigations, pContext);
        buildObjectStructure(pBaseUrl, ncc74656, allNCC74656Investigations, pContext);
    }

    /**
     * Generate digital objects for all sample files (currently there is only
     * one) of an investigation and store the data using the StagingRESTService.
     *
     * @param baseUrl The base URL of the staging REST service.
     * @param pStudy The parent study.
     * @param investigations The investigations for which the objects should be
     * created.
     * @param pContext The context to authorize the REST access.
     *
     * @throws Exception If anything goes wrong.
     */
    private void buildObjectStructure(String baseUrl, Study pStudy, Map<String, Investigation> investigations, SimpleRESTContext pContext) throws Exception {
        StagingRestClient stagingClient = new StagingRestClient(baseUrl + "/rest/staging/", pContext);
        BaseMetaDataRestClient baseMetadataClient = new BaseMetaDataRestClient(baseUrl + "/rest/basemetadata/", pContext);

        String tmp = System.getProperty("java.io.tmpdir");
        tmp += "/sampleData";

        Set<Entry<String, Investigation>> entries = investigations.entrySet();
        for (Entry<String, Investigation> entry : entries) {
            String doResource = "sampleData/" + pStudy.getTopic() + "/" + entry.getValue().getTopic() + "/image/";
            String dest = tmp + "/" + doResource;
            if (!new File(dest).exists()) {
                new File(dest).mkdirs();
            }
            //extract file
            LOGGER.debug("Trying to get resource " + doResource);
            URL u = URLCreator.cleanUrl(Thread.currentThread().getContextClassLoader().getResource(doResource));
            LOGGER.debug("Writing data from " + u + " to " + new File(dest + "/screen.jpg"));
            FileUtils.copyURLToFile(u, new File(dest + "/screen.jpg"));
            LOGGER.debug("Done. Starting ingest process.");
            LOGGER.debug("Creating DigitalObject");
            DigitalObject o = DigitalObject.factoryNewDigitalObject(pStudy.getTopic() + "-" + entry.getValue().getTopic());
            o.setLabel("Screen_" + entry.getValue().getTopic());
            o = baseMetadataClient.addDigitalObjectToInvestigation(entry.getValue().getInvestigationId(), o, Constants.USERS_GROUP_ID).getEntities().get(0);
            LOGGER.debug("Getting AccessPoint for staging.");
            long apId = stagingClient.getAllAccessPoints("USERS", pContext).getEntities().get(0).getId();
            String apIdentifier = stagingClient.getAccessPointById(apId, pContext).getEntities().get(0).getUniqueIdentifier();

            LOGGER.debug("Object with id " + o.getDigitalObjectIdentifier() + " created. Scheduling ingest.");
            IngestInformation info = stagingClient.createIngest(o.getDigitalObjectIdentifier(), apIdentifier).getEntities().get(0);
            try {
                stagingClient.createIngest("ShouldNotWork", apIdentifier);
                throw new Exception("Ingest for invalid object id seems to work. Cannot continue.");
            } catch (Exception e) {
                //Ok, error occured due to invalid object id. Just continue.
            }
            String stagingUrl = info.getStagingUrl();
            while (stagingUrl == null) {
                LOGGER.debug("Waiting for staging URL...");
                info = stagingClient.getIngestById(info.getId()).getEntities().get(0);
                stagingUrl = info.getStagingUrl();
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException ex) {
                }
            }
            LOGGER.debug("Ingest created, StagingURL available. Copying file " + new File(dest + "/screen.jpg") + " to " + stagingUrl);

            Configuration configuration = ProtocolSettings.getSingleton().getConfiguration("http");
            configuration.setProperty("authClass", "edu.kit.dama.test.AutoAuthentication");
            LOGGER.debug("Overwriting the default configuration with the specific configuration of generic ingest client.");

            AbstractFile f = new AbstractFile(new File(dest + "/screen.jpg"));
            f.uploadFileToFile(new AbstractFile(new URL(stagingUrl + "/data/screen.jpg"), configuration));

            stagingClient.updateIngest(info.getId(), null, INGEST_STATUS.PRE_INGEST_FINISHED.getId());
            LOGGER.debug("Done!");
        }
    }

    public void prepareDataDownload(String pBaseUrl, SimpleRESTContext pContext) throws Exception {
        prepareDownloads(pBaseUrl, allNX1Investigations, pContext);
        prepareDownloads(pBaseUrl, allNCC1701Investigations, pContext);
        prepareDownloads(pBaseUrl, allNCC1701DInvestigations, pContext);
        prepareDownloads(pBaseUrl, allNCC74656Investigations, pContext);
    }

    /**
     * Download the previously uploaded data from the server.
     *
     * @param pBaseUrl The REST base URL, e.g. "http://localhost:8080/KITDM"
     * @param pContext The REST context.
     *
     * @throws Exception if something goes wrong.
     */
    public void performDataDownload(String pBaseUrl, SimpleRESTContext pContext) throws Exception {
        performDownloads(pBaseUrl, nx1, allNX1Investigations, pContext);
        performDownloads(pBaseUrl, ncc1701, allNCC1701Investigations, pContext);
        performDownloads(pBaseUrl, ncc1701d, allNCC1701DInvestigations, pContext);
        performDownloads(pBaseUrl, ncc74656, allNCC74656Investigations, pContext);
    }

    /**
     * Prepare the download of the single object stored in investigations, which
     * are part of pStudy. The object id is determindes using study- and
     * investigation-topics. For access preparation, the access point with the
     * provided id will be used.
     *
     * @param pBaseUrl The REST base URL, e.g. http://localhost:8080/KITDM
     * @param investigations The investigations.
     * @param pAccessPointId The access point id.
     * @param pContext The rest context.
     */
    private void prepareDownloads(String pBaseUrl, Map<String, Investigation> investigations, SimpleRESTContext pContext) throws Exception {
        StagingRestClient stagingClient = new StagingRestClient(pBaseUrl + "/rest/staging/", pContext);
        BaseMetaDataRestClient mdClient = new BaseMetaDataRestClient(pBaseUrl + "/rest/basemetadata/", pContext);
        Set<Entry<String, Investigation>> entries = investigations.entrySet();
        for (Entry<String, Investigation> entry : entries) {
            Investigation inv = mdClient.getInvestigationById(entry.getValue().getInvestigationId(), "USERS").getEntities().get(0);
            DigitalObject o = mdClient.getDigitalObjectById(inv.getDataSets().toArray(new DigitalObject[]{})[0].getBaseId()).getEntities().get(0);
            LOGGER.debug("Getting AccessPoint for staging.");
            long apId = stagingClient.getAllAccessPoints("USERS", pContext).getEntities().get(0).getId();
            String apIdentifier = stagingClient.getAccessPointById(apId, pContext).getEntities().get(0).getUniqueIdentifier();

            DownloadInformation info = stagingClient.createDownload(o.getDigitalObjectIdentifier(), apIdentifier).getEntities().get(0);
            LOGGER.debug("Download with id {} created", info.getId());
        }
    }

    /**
     * Test the download of the single object stored in pInvestigation, which is
     * part of pStudy. The object id is determindes using study- and
     * investigation-topics. For access preparation, the access point with the
     * provided id will be used.
     *
     * @param pStudy The parent study.
     * @param pInvestigation The parent investigation.
     * @param pBaseUrl The REST base URL, e.g. http://localhost:8080/KITDM
     * @param pAccessPointId The access point id.
     * @param pContext The rest context.
     */
    private void performDownloads(String pBaseUrl, Study pStudy, Map<String, Investigation> investigations, SimpleRESTContext pContext) throws Exception {
        StagingRestClient client = new StagingRestClient(pBaseUrl + "/rest/staging/", pContext);
        BaseMetaDataRestClient mdClient = new BaseMetaDataRestClient(pBaseUrl + "/rest/basemetadata/", pContext);

        String tmp = System.getProperty("java.io.tmpdir");
        tmp += "/downloadData";

        Set<Entry<String, Investigation>> entries = investigations.entrySet();
        for (Entry<String, Investigation> entry : entries) {
            Investigation inv = mdClient.getInvestigationById(entry.getValue().getInvestigationId(), "USERS").getEntities().get(0);
            String dest = tmp + "/" + pStudy.getTopic() + "/" + inv.getTopic();
            if (!new File(dest).exists()) {
                new File(dest).mkdirs();
            }

            DigitalObject o = mdClient.getDigitalObjectById(inv.getDataSets().toArray(new DigitalObject[]{})[0].getBaseId()).getEntities().get(0);
            LOGGER.debug("Obtaining download for doi {}", o.getDigitalObjectId());
            DownloadInformation info = client.getAllDownloadInformation(null, o.getDigitalObjectIdentifier(), -1, 0, 1).getEntities().get(0);
            LOGGER.debug("Download with id {} obtained", info.getId());
            while (info.getStatus() != DOWNLOAD_STATUS.DOWNLOAD_READY.getId()) {
                LOGGER.debug("Wait...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                //reload download
                info = client.getDownloadById(info.getId()).getEntities().get(0);
                if (info.getStatus() == DOWNLOAD_STATUS.PREPARATION_FAILED.getId()) {
                    throw new Exception("Download preparation failed for transfer " + info.getTransferId());
                }
            }
            LOGGER.debug("Download available.");
            Configuration configuration = ProtocolSettings.getSingleton().getConfiguration("http");
            configuration.setProperty("authClass", "edu.kit.dama.test.AutoAuthentication");
            LOGGER.debug("Overwriting the default configuration with the specific configuration of generic ingest client.");
            AbstractFile f = new AbstractFile(new URL(info.getStagingUrl() + "/data/screen.jpg"), configuration);
            f.download(new AbstractFile(new File(dest + "/screen.jpg")));
        }
    }

    /**
     * Main entry point.
     *
     * @param args Program arguments.
     *
     * @throws java.lang.Exception If something fails.
     */
    public static void main(String[] args) throws Exception {
        AdalapiSettings.getSingleton().setOverwritePermission(AbstractFile.OVERWRITE_PERMISSION.ALLOWED);
        /**
         * delete all table content
         */
//    TRUNCATE ingestinformation CASCADE ;
//    TRUNCATE digitalobject_userdata CASCADE;
//    TRUNCATE digitalobject CASCADE;
//    TRUNCATE dataorganizationnode CASCADE;
//    TRUNCATE attribute CASCADE;
//    TRUNCATE investigation_participant CASCADE;
//    TRUNCATE participant CASCADE;
//    TRUNCATE investigation_metadataschema CASCADE;
//    TRUNCATE metadataschema CASCADE;
//    TRUNCATE investigation CASCADE;
//    TRUNCATE study_relation CASCADE;
//    TRUNCATE relation CASCADE;
//    TRUNCATE study CASCADE;
//    TRUNCATE organizationunit CASCADE;
//    DELETE FROM userdata WHERE userid > 1;
//    TRUNCATE task CASCADE;
//    DELETE FROM memberships WHERE user_id > 1;
//    DELETE FROM grants CASCADE;
//    DELETE FROM users WHERE id > 1;

        String restBaseURL = "http://localhost:8889/KITDM";
        SimpleRESTContext restContext = new SimpleRESTContext("admin", "dama14");

        TestExecutor testExecutor = new TestExecutor();

        BaseMetaDataRestClient testClient = new BaseMetaDataRestClient(restBaseURL + "/rest/basemetadata", restContext);
        LOGGER.debug("Check for availability of REST services");
        int cnt = 0;
        boolean running = false;
        while (cnt < 30) {
            LOGGER.debug("Checking service access...");
            if (testClient.checkService()) {
                LOGGER.debug("checkService() returned 'true' Let's start testing.");
                running = true;
                break;
            } else {
                LOGGER.debug("checkService() returned 'false'...let's wait one second.");
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException ex) {
                }
            }
            cnt++;
        }

        if (!running) {
            throw new Exception("Failed to connect to infrastructure within 30 seconds.");
        }

        LOGGER.debug("Storing metadata...");
        testExecutor.storeMetaData(restBaseURL, restContext);
        LOGGER.debug("Ingesting data...");
        testExecutor.ingestData(restBaseURL, restContext);

        /*LOGGER.debug("Starting TransferFinalizer inside docker...");
        String line = "docker exec tomcat sh /tmp/TransferFinalizer.sh -t INGEST";
        CommandLine cmdLine = CommandLine.parse(line);
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);

        int exitValue = executor.execute(cmdLine);
        System.out.println("TransferFinalizer process output:");
        System.out.println(outputStream.toString());

        if (exitValue != 0) {
            LOGGER.error("TransferFinalizer.sh failed!? Terminating.");
            System.exit(exitValue);
        } else {
            LOGGER.debug("TransferFinalizer succeeded. Continuing.");
        }
         */
        try {
            LOGGER.debug("Sleeping for 6 seconds to allow ingest finalization.");
            Thread.sleep(6000);
        } catch (InterruptedException e) {
        }

        LOGGER.debug("Preparing downloads...");
        testExecutor.prepareDataDownload(restBaseURL, restContext);

        /*LOGGER.debug("Starting TransferFinalizer inside docker...");
        line = "docker exec tomcat sh /tmp/TransferFinalizer.sh -t DOWNLOAD";
        cmdLine = CommandLine.parse(line);
        executor = new DefaultExecutor();
        outputStream = new ByteArrayOutputStream();
        streamHandler = new PumpStreamHandler(outputStream);

        executor.setStreamHandler(streamHandler);
        exitValue = executor.execute(cmdLine);

        System.out.println("TransferFinalizer process output:");
        System.out.println(outputStream.toString());

        if (exitValue != 0) {
            LOGGER.error("TransferFinalizer.sh failed!? Terminating.");
            System.exit(exitValue);
        } else {
            LOGGER.debug("TransferFinalizer succeeded. Continuing.");
        }*/
        try {
            LOGGER.debug("Sleeping for 7 seconds to allow download finalization.");
            Thread.sleep(7000);
        } catch (InterruptedException e) {
        }

        LOGGER.debug("Performing download...");
        testExecutor.performDataDownload(restBaseURL, restContext);
        LOGGER.debug("Finally, restoring metadata...");
        testExecutor.loadMetaData(restBaseURL, restContext);
        LOGGER.debug("All tests are done. Bye.");
    }
}
