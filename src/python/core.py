import __builtin__

# Prevent console from importing our classes (Security flaw)
def disallow_scriptrts_import():
    # Store old import function
    old_import = __builtin__.__import__

    # Define new import function which checks to make sure our classes aren't being imported
    def custom_importer(name, *args):
        if name.find("com.scriptrts") >= 0:
            raise ImportError("Importing ScriptRTS classes is disallowed")
        else:
            return apply(old_import, tuple([name]) + args)

    # Switch the import functions
    __builtin__.__import__ = custom_importer

# Stop the main thread (for Ctrl-C in console)
import dummy_thread
def interrupt():
    dummy_thread.interrupt_main()
    

