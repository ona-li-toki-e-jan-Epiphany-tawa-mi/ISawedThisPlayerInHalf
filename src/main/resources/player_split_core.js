var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");

/**
 * Logs a message to the console, showing the time and severity level with it.
 *
 * @param {string} level The severity level of the message (e.x. INFO, WARN, DEBUG.)
 * @param {string} message The message to log to the console.
 */
function logMessage(level, message) {
    var currentDate = new Date();

    print("[" + currentDate.getHours() + ":" + currentDate.getMinutes() + ":" + currentDate.getSeconds() + "] [PlayerSplitCore/" + level + "]: " + message);
}

/**
 * Creates a new method signature from the name of the method and its description.
 *
 * @param {string} methodName The name of the method.
 * @param {string} description The description of the method.
 *
 * @returns {object} A new method signature.
 */
function createMethodSignature(methodName, description) {
    return {
        name: ASMAPI.mapMethod(methodName),
        desc: description,

        equals: function (method) {
            return method.name === this.name && method.desc === this.desc;
        }
    };
}

/**
 * Tries to find a method within classNode with a signature equal to methodSignature.
 * Returns null if nothing is found.
 *
 * @param {object} classNode The class node to search through.
 * @param {object} methodSignature The method signature to find.
 *
 * @returns {object/null} The found method node, or null, if nothing is found.
 */
function findMethodWithSignature(classNode, methodSignature) {
    for (var i in classNode.methods) {
        var method = classNode.methods[i];

        if (methodSignature.equals(method))
            return method;
    }

    return null;
}

function initializeCoreMod() {
    return {
        "Entity": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.entity.Entity"
            },

            "transformer": function(classNode) {
                var pickMethod = findMethodWithSignature(classNode, createMethodSignature("pick", "(DFZ)Lnet/minecraft/util/math/RayTraceResult;"));

                if (pickMethod !== null) {
                    try {
                        var oldInstructions = pickMethod.instructions;
                        var success = false;

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i);

                            if (instruction.getOpcode() === Opcodes.INVOKEVIRTUAL && instruction.name === "getEyePosition" && instruction.desc === "(F)Lnet/minecraft/util/math/Vec3d;") {
                                var newInstructions = new InsnList();

                                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                newInstructions.add(new InsnNode(Opcodes.SWAP));
                                newInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "offsetRaycast",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ));

                                oldInstructions.insert(instruction, newInstructions);

                                logMessage("INFO", "Successfully transformed pick function in net.minecraft.entity.Entity");
                                success = true;

                                break;
                            }
                        }

                        if (!success)
                            logMessage("ERROR", "An error occurred while transforming pick function in net.minecraft.entity.Entity:\n    Unable to find injection point in pick() of net.minecraft.entity.Entity");

                    } catch (exception) {
                        logMessage("ERROR", "An error occurred while transforming pick function in net.minecraft.entity.Entity:\n    " + exception);
                        transformsFailed = true;
                    }

                } else {
                    logMessage("ERROR", "An error occurred while transforming pick function in net.minecraft.entity.Entity:\n    Unable to find function to transform");
                    transformsFailed = true;
                }

                return classNode;
            }
        }
    }
}