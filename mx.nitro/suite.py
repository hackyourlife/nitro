suite = {
  "mxversion" : "5.175.4",
  "name" : "nitro",
  "versionConflictResolution" : "latest",

  "javac.lint.overrides" : "none",

  "imports" : {
    "suites" : [
      {
        "name" : "vmx86",
        "version" : "0bf2f0102017d009063842c2a901ff75be14d8f5",
        "urls" : [
          {"url" : "https://github.com/pekd/vmx86", "kind" : "git"},
        ]
      },
    ],
  },

  "licenses" : {
    "GPLv3" : {
      "name" : "GNU General Public License, version 3",
      "url" : "https://opensource.org/licenses/GPL-3.0",
    }
  },

  "defaultLicense" : "GPLv3",

  "projects" : {

    "org.graalvm.vm.trcview.arch.arm" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "vmx86:VMX86_TRCVIEW",
      ],
      "javaCompliance" : "1.8+",
      "workingSets" : "vmx86",
      "license" : "GPLv3",
    },

  },

  "distributions" : {
    "TRCVIEW_ARM_PLUGIN" : {
      "path" : "build/nitro.jar",
      "sourcesPath" : "build/nitro.src.zip",
      "subDir" : "nitro",
      "dependencies" : [
        "org.graalvm.vm.trcview.arch.arm",
      ],
      "distDependencies" : [
        "vmx86:VMX86_TRCVIEW",
      ],
      "license" : "GPLv3",
    },

    "TRCVIEW_ARM" : {
      "path" : "build/trcview.jar",
      "sourcesPath" : "build/trcview.src.zip",
      "subDir" : "nitro",
      "mainClass" : "org.graalvm.vm.trcview.ui.MainWindow",
      "dependencies" : [
        "org.graalvm.vm.trcview.arch.arm",
      ],
      "license" : "GPLv3",
    }
  }
}
