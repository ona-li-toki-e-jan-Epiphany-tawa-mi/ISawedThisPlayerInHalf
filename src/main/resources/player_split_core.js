var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");



/**
 * Checks if a instruction node has the given opcode.
 *
 * @param {object} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 */
function checkInsn(instructionNode, opCode) {
    return instructionNode.getOpcode() === opCode;
}

/**
 * Checks if a instruction node has the given opcode and index.
 *
 * @param {object} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 * @param {number} varIndex The index of the constant pool that the instruction should have.
 */
function checkVarInsn(instructionNode, opCode, varIndex) {
    return instructionNode.getOpcode() === opCode && instructionNode.var === varIndex;
}

/**
 * Checks if a method instruction node has the given opcode, name, and descriptor.
 *
 * @param {object} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 * @param {string} name The name the instruction should have.
 * @param {string} descriptor The descriptor the instruction should have.
 */
function checkMethodInsn(instructionNode, opCode, name, descriptor) {
    return instructionNode.getOpcode() === opCode && instructionNode.name === name && instructionNode.desc === descriptor;
}



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

                        if (checkMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "getEyePosition", "(F)Lnet/minecraft/util/math/Vec3d;")) {
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

                        if (checkMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "getEyePosition", "(F)Lnet/minecraft/util/math/Vec3d;")) {
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

                    for (var i = 0; i < oldInstructions.size(); i++) {
                        var instruction = oldInstructions.get(i);

                        if (checkMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "isThirdPerson", "()Z")) {
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

        "AbstractArrowEntity": {
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

                    for (var i = 0; i < oldInstructions.size(); i++) {
                        var instruction = oldInstructions.get(i);

                        if (checkMethodInsn(instruction, Opcodes.INVOKESPECIAL, "<init>", "(Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V")) {
                            var newInstructions = new InsnList();

                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            newInstructions.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                "offsetProjectile",
                                "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;)V",
                                false
                            ));

                            oldInstructions.insert(instruction, newInstructions);

                            success = true;
                            logMessage("INFO", "Successfully transformed constructor in net.minecraft.entity.projectile.AbstractArrowEntity");

                            break;
                        }
                    }

                    if (!success)
                        logMessage("ERROR", "An error occurred while transforming constructor in net.minecraft.entity.projectile.AbstractArrowEntity:\n    Unable to find injection point");

                } catch (exception) {
                    logMessage("ERROR", "An error occurred while transforming constructor in net.minecraft.entity.projectile.AbstractArrowEntity:\n    " + exception);
                }

                return methodNode;
            }
        },

        "ThrowableEntity": {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.entity.projectile.ThrowableEntity",
                "methodName": "<init>",
                "methodDesc": "(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;)V"
            },

            "transformer": function(methodNode) {
                try {
                    var oldInstructions = methodNode.instructions;
                    var success = false;

                    for (var i = 0; i < oldInstructions.size(); i++) {
                        var instruction = oldInstructions.get(i);

                        if (checkMethodInsn(instruction, Opcodes.INVOKESPECIAL, "<init>", "(Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V")) {
                            var newInstructions = new InsnList();

                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            newInstructions.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                "offsetProjectile",
                                "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;)V",
                                false
                            ));

                            oldInstructions.insert(instruction, newInstructions);

                            success = true;
                            logMessage("INFO", "Successfully transformed constructor in net.minecraft.entity.projectile.ThrowableEntity");

                            break;
                        }
                    }

                    if (!success)
                        logMessage("ERROR", "An error occurred while transforming constructor in net.minecraft.entity.projectile.ThrowableEntity:\n    Unable to find injection point");

                } catch (exception) {
                    logMessage("ERROR", "An error occurred while transforming constructor in net.minecraft.entity.projectile.ThrowableEntity:\n    " + exception);
                }

                return methodNode;
            }
        },

        "EnderEyeItem": {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.item.EnderEyeItem",
                "methodName": "onItemRightClick",
                "methodDesc": "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"
            },

            "transformer": function(methodNode) {
                try {
                    var oldInstructions = methodNode.instructions;
                    var success = false;

                    for (var i = 0; i < oldInstructions.size(); i++) {
                        var instruction = oldInstructions.get(i);

                        if (checkMethodInsn(instruction, Opcodes.INVOKESPECIAL, "<init>", "(Lnet/minecraft/world/World;DDD)V")) {
                            var newInstructions = new InsnList();

                            newInstructions.add(new InsnNode(Opcodes.DUP));
                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            newInstructions.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                "offsetProjectile",
                                "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;)V",
                                false
                            ));

                            oldInstructions.insert(instruction, newInstructions);

                            success = true;
                            logMessage("INFO", "Successfully transformed onItemRightClick function in net.minecraft.item.EnderEyeItem");

                            break;
                        }
                    }

                    if (!success)
                        logMessage("ERROR", "An error occurred while transforming onItemRightClick function in net.minecraft.item.EnderEyeItem:\n    Unable to find injection point");

                } catch (exception) {
                    logMessage("ERROR", "An error occurred while transforming onItemRightClick function in net.minecraft.item.EnderEyeItem:\n    " + exception);
                }

                return methodNode;
            }
        },

        "FishingRodItem": {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.item.FishingRodItem",
                "methodName": "onItemRightClick",
                "methodDesc": "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"
            },

            "transformer": function(methodNode) {
                try {
                    var oldInstructions = methodNode.instructions;
                    var success = false;

                    for (var i = 0; i < oldInstructions.size(); i++) {
                        var instruction = oldInstructions.get(i);

                        if (checkMethodInsn(instruction, Opcodes.INVOKESPECIAL, "<init>", "(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;II)V")) {
                            var newInstructions = new InsnList();

                            newInstructions.add(new InsnNode(Opcodes.DUP));
                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            newInstructions.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                "offsetProjectile",
                                "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;)V",
                                false
                            ));

                            oldInstructions.insert(instruction, newInstructions);

                            success = true;
                            logMessage("INFO", "Successfully transformed onItemRightClick function in net.minecraft.item.FishingRodItem");

                            break;
                        }
                    }

                    if (!success)
                        logMessage("ERROR", "An error occurred while transforming onItemRightClick function in net.minecraft.item.FishingRodItem:\n    Unable to find injection point");

                } catch (exception) {
                    logMessage("ERROR", "An error occurred while transforming onItemRightClick function in net.minecraft.item.FishingRodItem:\n    " + exception);
                }

                return methodNode;
            }
        },

        "FishRenderer": {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.client.renderer.entity.FishRenderer",
                "methodName": "render",
                "methodDesc": "(Lnet/minecraft/entity/projectile/FishingBobberEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V"
            },

            "transformer": function(methodNode) {
                try {
                    var oldInstructions = methodNode.instructions;
                    var success = false;

                    for (var i = 0; i < oldInstructions.size() - 21; i++) {
                        var instruction = oldInstructions.get(i);

                        if (checkVarInsn(instruction, Opcodes.DLOAD, 25)) {
                            var instructions = [instruction];

                            for (var k = 1; k < 21; k++) {
                                var potentialInstruction = oldInstructions.get(i + k);

                                if (potentialInstruction.getOpcode() !== -1)
                                    instructions.push(potentialInstruction);
                            }

                            if (checkVarInsn(instructions[1], Opcodes.DLOAD, 32) && checkInsn(instructions[2], Opcodes.DSUB) && checkInsn(instructions[3], Opcodes.D2F) && checkVarInsn(instructions[4], Opcodes.FSTORE, 38) &&
                                    checkVarInsn(instructions[5], Opcodes.DLOAD, 27) && checkVarInsn(instructions[6], Opcodes.DLOAD, 34) && checkInsn(instructions[7], Opcodes.DSUB) && checkInsn(instructions[8], Opcodes.D2F) && checkVarInsn(instructions[9], Opcodes.FLOAD, 31) && checkInsn(instructions[10], Opcodes.FADD) && checkVarInsn(instructions[11], Opcodes.FSTORE, 39) &&
                                    checkVarInsn(instructions[12], Opcodes.DLOAD, 29) && checkVarInsn(instructions[13], Opcodes.DLOAD, 36) && checkInsn(instructions[14], Opcodes.DSUB) && checkInsn(instructions[15], Opcodes.D2F) && checkVarInsn(instructions[16], Opcodes.FSTORE, 40)) {
                                var getVectorList = new InsnList();
                                var addXList = new InsnList();
                                var addYList = new InsnList();
                                var addZList = new InsnList();


                                getVectorList.add(new VarInsnNode(Opcodes.ALOAD, 7));
                                getVectorList.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "getOffsets",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ));
                                getVectorList.add(new InsnNode(Opcodes.DUP));
                                getVectorList.add(new InsnNode(Opcodes.DUP));

                                addXList.add(new InsnNode(Opcodes.DUP2_X1));
                                addXList.add(new InsnNode(Opcodes.POP2));
                                addXList.add(new FieldInsnNode(
                                    Opcodes.GETFIELD,
                                    "net/minecraft/util/math/Vec3d",
                                    "x",
                                    "D"
                                ));
                                addXList.add(new InsnNode(Opcodes.DADD));

                                addYList.add(new InsnNode(Opcodes.DUP2_X1));
                                addYList.add(new InsnNode(Opcodes.POP2));
                                addYList.add(new FieldInsnNode(
                                    Opcodes.GETFIELD,
                                    "net/minecraft/util/math/Vec3d",
                                    "y",
                                    "D"
                                ));
                                addYList.add(new InsnNode(Opcodes.DADD));

                                addZList.add(new InsnNode(Opcodes.DUP2_X1));
                                addZList.add(new InsnNode(Opcodes.POP2));
                                addZList.add(new FieldInsnNode(
                                    Opcodes.GETFIELD,
                                    "net/minecraft/util/math/Vec3d",
                                    "z",
                                    "D"
                                ));
                                addZList.add(new InsnNode(Opcodes.DADD));


                                // DLOAD 25
                                oldInstructions.insertBefore(instructions[0], getVectorList);
                                // DSUB
                                oldInstructions.insert(instructions[2], addXList);
                                // DSUB
                                oldInstructions.insert(instructions[7], addYList);
                                // DSUB
                                oldInstructions.insert(instructions[14], addZList);

                                success = true;
                                logMessage("INFO", "Successfully transformed render function in net.minecraft.client.renderer.FishRenderer");

                                break;
                            }
                        }
                    }

                    if (!success)
                        logMessage("ERROR", "An error occurred while transforming render function in net.minecraft.client.renderer.FishRenderer:\n    Unable to find injection points");

                } catch (exception) {
                    logMessage("ERROR", "An error occurred while transforming render function in net.minecraft.client.renderer.FishRenderer:\n    " + exception);
                }

                return methodNode;
            }
        }
    }
}