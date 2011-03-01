import __builtin__

def disallow_scriptrts_import():
    old_import = __builtin__.__import__
    def custom_importer(name, *args):
        if name.find("com.scriptrts") >= 0:
            raise ImportError("Importing ScriptRTS classes is disallowed")
        else:
            return apply(old_import, tuple([name]) + args)
    __builtin__.__import__ = custom_importer
