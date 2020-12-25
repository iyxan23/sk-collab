package com.iyxan23.sketch.collab.models

// These strings are all should be decrypted in it's raw format

class SketchwareProject(
        var project: String,
        var logic: String,
        var view: String,
        var resource: String,
        var library: String,
        var file: String,

        var metadata: SketchwareProjectMeta
)