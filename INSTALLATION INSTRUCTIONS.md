# Github Installation

- Install Github Desktop Client

- Clone our forked repository: [drbo6s-Forge](https://github.com/drbo6/drbo6s-Forge.git)

# Java Eclipse Installation

- Install JDK22 and Eclipse. Install everything following the readme.md instructions.

- Install Java SE Development Kit 8u401
  
  - [Java Archive | Oracle](https://www.oracle.com/java/technologies/downloads/archive/)

- Add the following line to Eclipse.ini (C:\Users[username]\eclipse\java-[release date]\eclipse) BEFORE the --vmargs if Eclipse no longer runs (and verify that the path exists):
  
  `-vm C:\Program Files\Java\jdk-22\bin\server\jvm.dll`
  
  - This will make it use JDK so it can run. Otherwise it will go through 8 which is too old. However, we need 8 to build the adventure and android files.

- Next, open Eclipse and go to Window > Preferences > Java, and click on the > in front of Java in the tree to select "Installed JREs".

- Click on Add and then Browse, click Next, and select the JDK8 folder (in Program Files/Java).

- Select the JDK8 version only, and apply and close.

# Creating the project

- Create a workspace. Go to the workbench. Right-click inside of Package Explorer > Import... > Maven > Existing Maven Projects > Navigate to root path of the local forge repo and ensure everything is checked > Finish.
  
  - Close the welcome window
  
  - Window > Show View > Package Explorer
  
  - Import projects
  
  - Maven (click on >)
  
  - Existing Maven projects
  
  - Select the directory (github is usually in documents) and go

- Let Eclipse run through building the project. You may be prompted for resolving any missing Maven plugins -- accept the ones offered. You may see errors appear in the "Problems" tab. These should be automatically resolved as plug-ins are installed and Eclipse continues the build process. If this is the first time for some plug-in installs, Eclipse may prompt you to restart. Do so. Be patient for this first time through.

- Once everything builds, all errors should disappear. You can now advance to Project launch.
  
  - If you get the forge.lda error, just right click it and select the quick fix that will change the line to main.java.forge.lda

# Building the project for playing

1. Right click forge (the root) in the project explorer and click Run as > Maven Build.

2. Add the following in the configuration:
   
   - Goals: `clean install`
   
   - Profiles: `windows-linux`

3- The build will end up in forge-gui-desktop/target.

- Right click and Show in > System Explorer for easy access.

- Extract the forge-gui-desktop-x.y.z-SNAPSHOT.tar.bz2 to get all the files.

# Building the project for testing

For quicker test builds, right click the gui-desktop and select Run as Java Application. Then type in Forge at the top of the selection and select main forge.view.

# Updating the Github fork

Using the Desktop client, you manually have to swap out the upstream repository between:

- Our fork: `https://github.com/drbo6/drbo6s-Forge.git`

- The original repository: `https://github.com/Card-Forge/forge.git`

Don't worry about pushing changes to the original accidentally, you do not have writing access and will never be able to do so.

The steps are:

1. Switch to original repository and fetch origin

2. Review the files that you changed and reject any changes of your code
   
   - **LDAModelGenerator.java** - Throws up an error and setting line 1 to main.java.forge.lda fixed that.
   
   - **GamePlayerUtil.java** - Injected code that will put override the names used for draft opponents.
   
   - **Various cards and customs in Cardfolder**
     
     - bob_dark_confidant.txt
     
     - captain_bradleys_sword.txt
     
     - chloe_deep_forest_hermit.txt
     
     - chloe_deranged_hermit.txt
     
     - forth_corgis.txt
     
     - kudo_king_among_corgis.txt
     
     - lord_reginald_corgi_of_the_people.txt
     
     - nicol_bolas_the_ravager_nicol_bolas_the_arisen.txt
     
     - reggie_stellar_pup.txt
     
     - sir_reginald_menace_at_the_door

3. Switch back to your own repository and commit

4. Push to origin
