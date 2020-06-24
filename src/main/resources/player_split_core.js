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

function initializeCoreMod() {
    return {
        "Entity": {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.entity.Entity",
                "methodName": "pick",
                "methodDesc": "(DFZ)Lnet/minecraft/util/math/RayTraceResult;"
            },

            "transformer": function(methodNode) {
                try {
                    var oldInstructions = methodNode.instructions;
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

                return methodNode;
            }
        },

        "GameRenderer": {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.client.renderer.GameRenderer",
                "methodName": "getMouseOver",
                "methodDesc": "(F)V"
            },

            "transformer": function(methodNode) {
                try {
                    var oldInstructions = methodNode.instructions;
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

                return methodNode;
            }
        },

        "FirstPersonRenderer": {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.client.renderer.FirstPersonRenderer",
                "methodName": "renderItemInFirstPerson",
                "methodDesc": "(FLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;Lnet/minecraft/client/entity/player/ClientPlayerEntity;I)V"
            },

            "transformer": function(methodNode) {
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


                    methodNode.instructions.insert(newInstructions);
                    logMessage("INFO", "Successfully transformed renderItemInFirstPerson function in net.minecraft.client.renderer.FirstPersonRenderer");

                } catch (exception) {
                    logMessage("ERROR", "An error occurred while transforming renderItemInFirstPerson function in net.minecraft.client.renderer.FirstPersonRenderer:\n    " + exception);
                }

                return methodNode;
            }
        },

        "WorldRenderer": {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.client.renderer.WorldRenderer",
                "methodName": "updateCameraAndRender",
                "methodDesc": "(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/renderer/Matrix4f;)V"
            },

            "transformer": function(methodNode) {
                try {
                    var oldInstructions = methodNode.instructions;
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

                return methodNode;
            }
        },

        /*"AbstractArrowEntity": {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.entity.projectile.AbstractArrowEntity",
                "methodName": "<init>",
                "methodDesc": "(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;)V"
            },

            "transformer": function(methodNode) {
                try {
                    var oldInstructions = methodNode.instructions;
                    var success = false;

                    for (var i = 1; i < oldInstructions.size(); i++) {
                        var instruction = oldInstructions.get(i);

                        if (instruction.getOpcode() === Opcodes.INVOKEVIRTUAL && instruction.name === "getPosX" && instruction.desc === "()D") {
                            var newInstructions = new InsnList();

                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            newInstructions.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                "offsetProjectile",
                                "(Lnet/minecraft/entity/projectile/AbstractArrowEntity;Lnet/minecraft/entity/LivingEntity;)V",
                                false
                            ));

                            oldInstructions.insert(instruction, newInstructions);

                            success = true;
                            logMessage("INFO", "Successfully transformed updateCameraAndRender function in net.minecraft.entity.projectile.AbstractArrowEntity");

                            break;
                        }
                    }

                    if (!success)
                        logMessage("ERROR", "An error occurred while transforming updateCameraAndRender function in net.minecraft.entity.projectile.AbstractArrowEntity:\n    Unable to find injection point");

                } catch (exception) {
                    logMessage("ERROR", "An error occurred while transforming updateCameraAndRender function in net.minecraft.entity.projectile.AbstractArrowEntity:\n    " + exception);
                }

                return methodNode;
            }
        }*/
    }
}