package com.iyxan23.sketch.collab.models

class SketchwareProjectMeta(var name: String        ,
                            var version: String     ,
                            var packageName: String ,
                            var projectName: String ,
                            var id: Int             ) {

    override fun toString(): String {
        return "$projectName  - $packageName ($id)"
    }
}