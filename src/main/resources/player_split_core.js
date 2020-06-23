var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
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
                                newInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "offsetRaycast",
                                    "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
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
                    }

                } else
                    logMessage("ERROR", "An error occurred while transforming pick function in net.minecraft.entity.Entity:\n    Unable to find function to transform");

                return classNode;
            }
        },

        "GameRenderer": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.client.renderer.GameRenderer"
            },

            "transformer": function(classNode) {
                var getMouseOverMethod = findMethodWithSignature(classNode, createMethodSignature("getMouseOver", "(F)V"));

                if (getMouseOverMethod !== null) {
                    try {
                        var oldInstructions = getMouseOverMethod.instructions;
                        var success = false;

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i);

                            if (instruction.getOpcode() === Opcodes.INVOKEVIRTUAL && instruction.name === "getEyePosition" && instruction.desc === "(F)Lnet/minecraft/util/math/Vec3d;") {
                                var newInstructions = new InsnList();

                                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
                                newInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "offsetRaycast",
                                    "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ));

                                oldInstructions.insert(instruction, newInstructions);

                                logMessage("INFO", "Successfully transformed getMouseOver function in net.minecraft.client.renderer.GameRenderer");
                                success = true;

                                break;
                            }
                        }

                        if (!success)
                            logMessage("ERROR", "An error occurred while transforming getMouseOver function in net.minecraft.client.renderer.GameRenderer:\n    Unable to find injection point");

                    } catch (exception) {
                        logMessage("ERROR", "An error occurred while transforming getMouseOver function in net.minecraft.client.renderer.GameRenderer:\n    " + exception);
                    }

                } else
                    logMessage("ERROR", "An error occurred while transforming getMouseOver function in net.minecraft.client.renderer.GameRenderer:\n    Unable to find function to transform");

                return classNode;
            }
        },

        "FirstPersonRenderer": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.client.renderer.FirstPersonRenderer"
            },

            "transformer": function(classNode) {
                var renderItemInFirstPerson = findMethodWithSignature(classNode, createMethodSignature("renderItemInFirstPerson", "(FLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;Lnet/minecraft/client/entity/player/ClientPlayerEntity;I)V"));

                if (renderItemInFirstPerson !== null) {
                    try {
                        var newInstructions = new InsnList();
                        var skipReturn = new LabelNode();


                        newInstructions.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "com/epiphany/isawedthisplayerinhalf/rendering/RenderingOffsetter",
                            "shouldRenderHand",
                            "()Z",
                            false
                        ));
                        newInstructions.add(new JumpInsnNode(Opcodes.IFNE, skipReturn));

                        newInstructions.add(new InsnNode(Opcodes.RETURN));

                        newInstructions.add(skipReturn);


                        renderItemInFirstPerson.instructions.insert(newInstructions);
                        logMessage("INFO", "Successfully transformed renderItemInFirstPerson function in net.minecraft.client.renderer.FirstPersonRenderer");

                    } catch (exception) {
                        logMessage("ERROR", "An error occurred while transforming renderItemInFirstPerson function in net.minecraft.client.renderer.FirstPersonRenderer:\n    " + exception);
                    }

                } else
                    logMessage("ERROR", "An error occurred while transforming renderItemInFirstPerson function in net.minecraft.client.renderer.FirstPersonRenderer:\n    Unable to find function to transform");

                return classNode;
            }
        },

        "WorldRenderer": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.client.renderer.WorldRenderer"
            },

            "transformer": function(classNode) {
                var updateCameraAndRender = findMethodWithSignature(classNode, createMethodSignature("updateCameraAndRender", "(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/renderer/Matrix4f;)V"));

                if (updateCameraAndRender !== null) {
                    try {
                        var oldInstructions = updateCameraAndRender.instructions;
                        var success = false;

                        for (var i = 1; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i);

                            if (instruction.getOpcode() === Opcodes.INVOKEVIRTUAL && instruction.name === "isThirdPerson" && instruction.desc === "()Z") {
                                oldInstructions.insert(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/rendering/RenderingOffsetter",
                                    "modifiedIsThirdPerson",
                                    "(Lnet/minecraft/client/renderer/ActiveRenderInfo;)Z",
                                    false
                                ));
                                oldInstructions.remove(instruction);

                                success = true;
                                logMessage("INFO", "Successfully transformed updateCameraAndRender function in net.minecraft.client.renderer.WorldRenderer");

                                break;
                            }
                        }

                        if (!success)
                            logMessage("ERROR", "An error occurred while transforming updateCameraAndRender function in net.minecraft.client.renderer.WorldRenderer:\n    Unable to find injection point");

                    } catch (exception) {
                        logMessage("ERROR", "An error occurred while transforming updateCameraAndRender function in net.minecraft.client.renderer.WorldRenderer:\n    " + exception);
                    }

                } else
                    logMessage("ERROR", "An error occurred while transforming updateCameraAndRender function in net.minecraft.client.renderer.WorldRenderer:\n    Unable to find function to transform");

                return classNode;
            }
        }
    }
}