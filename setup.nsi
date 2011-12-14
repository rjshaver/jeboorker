# Auto-generated by EclipseNSIS Script Wizard
# 02.08.2011 13:26:22

Name Jeboorker

# General Symbol Definitions
!define REGKEY "SOFTWARE\$(^Name)"
!define COMPANY ""
!define URL ""

# MUI Symbol Definitions
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install-colorful.ico"
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_STARTMENUPAGE_REGISTRY_ROOT HKLM
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_STARTMENUPAGE_REGISTRY_KEY ${REGKEY}
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME StartMenuGroup
!define MUI_STARTMENUPAGE_DEFAULTFOLDER Jeboorker
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall-colorful.ico"
!define MUI_UNFINISHPAGE_NOAUTOCLOSE

# Included files
!include Sections.nsh
!include MUI2.nsh

# Variables
Var StartMenuGroup

# Installer pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU Application $StartMenuGroup
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

# Installer languages
!insertmacro MUI_LANGUAGE German

# Installer attributes
OutFile setup.exe
InstallDir $PROGRAMFILES\Jeboorker
CRCCheck on
XPStyle on
ShowInstDetails hide
VIProductVersion 0.2.0.0
VIAddVersionKey ProductName Jeboorker
VIAddVersionKey ProductVersion "${VERSION}"
VIAddVersionKey FileVersion "${VERSION}"
VIAddVersionKey FileDescription ""
VIAddVersionKey LegalCopyright ""
InstallDirRegKey HKLM "${REGKEY}" Path
ShowUninstDetails show

# Installer sections
Section -Main SEC0000
    SetOverwrite on
    SetOutPath $INSTDIR
    File Jeboorker32.exe
    File Jeboorker64.exe
    File Jeboorker32.lap
    File Jeboorker64.lap
    File msvcr71.dll
    File msvcr100.dll
    File Readme.txt
    
    SetOutPath $INSTDIR\lib
    File lib\bcprov-jdk16-146.jar
    File lib\commons-io-2.0.jar
    File lib\commons-lang-2.5.jar
    File lib\iText-5.0.6.jar
    File lib\jeboorker.jar
    File lib\jsoup-1.5.2.jar
    File lib\jsoup-1.5.2-sources.jar
    
    SetOutPath $INSTDIR\lib\orientdb
    File lib\orientdb\jpa.jar
    File lib\orientdb\orient-commons-1.0rc3.jar
    File lib\orientdb\orientdb-core-1.0rc3.jar
    
    SetOutPath $INSTDIR\lib\epubcheck
    File lib\epubcheck\epubcheck-1.2.jar
    File lib\epubcheck\jing.jar
    File lib\epubcheck\saxon.jar
    WriteRegStr HKLM "${REGKEY}\Components" Main 1
SectionEnd

Section -post SEC0001
    WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
    SetOutPath $INSTDIR
    WriteUninstaller $INSTDIR\uninstall.exe
    !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    SetOutPath $SMPROGRAMS\$StartMenuGroup
    CreateShortCut "$SMPROGRAMS\$StartMenuGroup\Jeboorker32.lnk" "$INSTDIR\Jeboorker32.exe"
    CreateShortCut "$SMPROGRAMS\$StartMenuGroup\Jeboorker64.lnk" "$INSTDIR\Jeboorker64.exe"
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Uninstall $(^Name).lnk" $INSTDIR\uninstall.exe
    !insertmacro MUI_STARTMENU_WRITE_END
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayName "$(^Name)"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayIcon $INSTDIR\uninstall.exe
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" UninstallString $INSTDIR\uninstall.exe
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoModify 1
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoRepair 1
SectionEnd

# Macro for selecting uninstaller sections
!macro SELECT_UNSECTION SECTION_NAME UNSECTION_ID
    Push $R0
    ReadRegStr $R0 HKLM "${REGKEY}\Components" "${SECTION_NAME}"
    StrCmp $R0 1 0 next${UNSECTION_ID}
    !insertmacro SelectSection "${UNSECTION_ID}"
    GoTo done${UNSECTION_ID}
next${UNSECTION_ID}:
    !insertmacro UnselectSection "${UNSECTION_ID}"
done${UNSECTION_ID}:
    Pop $R0
!macroend

# Uninstaller sections
Section /o -un.Main UNSEC0000
    Delete /REBOOTOK $INSTDIR\lib\epubcheck\saxon.jar
    Delete /REBOOTOK $INSTDIR\lib\epubcheck\jing.jar
    Delete /REBOOTOK $INSTDIR\lib\epubcheck\epubcheck-1.2.jar
    RMDir  $INSTDIR\lib\epubcheck
    Delete /REBOOTOK $INSTDIR\lib\orientdb\orientdb-core-1.0rc3.jar
    Delete /REBOOTOK $INSTDIR\lib\orientdb\orient-commons-1.0rc3.jar
    Delete /REBOOTOK $INSTDIR\lib\orientdb\jpa.jar
    RMDir  $INSTDIR\lib\orientdb
    Delete /REBOOTOK $INSTDIR\lib\jsoup-1.5.2-sources.jar
    Delete /REBOOTOK $INSTDIR\lib\jsoup-1.5.2.jar
    Delete /REBOOTOK $INSTDIR\lib\jeboorker.jar
    Delete /REBOOTOK $INSTDIR\lib\iText-5.0.6.jar
    Delete /REBOOTOK $INSTDIR\lib\commons-lang-2.5.jar
    Delete /REBOOTOK $INSTDIR\lib\commons-io-2.0.jar
    Delete /REBOOTOK $INSTDIR\lib\bcprov-jdk16-146.jar
    RMDir  $INSTDIR\lib
    
    Delete /REBOOTOK $INSTDIR\Jeboorker32.exe
    Delete /REBOOTOK $INSTDIR\Jeboorker64.exe
    Delete /REBOOTOK $INSTDIR\Jeboorker32.lap
    Delete /REBOOTOK $INSTDIR\Jeboorker64.lap
    Delete /REBOOTOK $INSTDIR\msvcr71.dll
    Delete /REBOOTOK $INSTDIR\msvcr100.dll
    Delete /REBOOTOK $INSTDIR\Readme.txt
    
    RMDir $INSTDIR
    DeleteRegValue HKLM "${REGKEY}\Components" Main
SectionEnd

Section -un.post UNSEC0001
    DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Uninstall $(^Name).lnk"
    Delete /REBOOTOK $INSTDIR\uninstall.exe
    DeleteRegValue HKLM "${REGKEY}" StartMenuGroup
    DeleteRegValue HKLM "${REGKEY}" Path
    DeleteRegKey /IfEmpty HKLM "${REGKEY}\Components"
    DeleteRegKey /IfEmpty HKLM "${REGKEY}"
    RmDir /REBOOTOK $SMPROGRAMS\$StartMenuGroup
    RmDir /REBOOTOK $INSTDIR
SectionEnd

# Installer functions
Function .onInit
    InitPluginsDir
FunctionEnd

# Uninstaller functions
Function un.onInit
    ReadRegStr $INSTDIR HKLM "${REGKEY}" Path
    !insertmacro MUI_STARTMENU_GETFOLDER Application $StartMenuGroup
    !insertmacro SELECT_UNSECTION Main ${UNSEC0000}
FunctionEnd

