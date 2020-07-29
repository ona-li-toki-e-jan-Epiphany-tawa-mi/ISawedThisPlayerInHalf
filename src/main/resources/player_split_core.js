var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");



/**
 * Tries to find a method within the given class.
 * Returns null if nothing is found.
 *
 * @param {object} classNode The class node to search through.
 * @param {string} methodName The name of the method to find.
 * @param {string} descriptor The descriptor of the method to find.
 *
 * @returns {object/null} The found method. Or null, if nothing is found.
 */
function findMethodWithSignature(classNode, methodName, descriptor) {
    for (var i in classNode.methods) {
        var method = classNode.methods[i];

        if (method.name === methodName && method.desc === descriptor)
            return method;
    }

    return null;
}

/**
 * Checks if a field instruction node has the given opcode, owner, name, and descriptor.
 *
 * @param {object} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 * @param {string} name The owner the instruction should have.
 * @param {string} name The name the instruction should have.
 * @param {string} descriptor The descriptor the instruction should have.
 */
function checkFieldInsn(instructionNode, opCode, owner, name, descriptor) {
    return instructionNode.getOpcode() === opCode && instructionNode.owner === owner && instructionNode.name === name && instructionNode.desc === descriptor;
}

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
 * Checks if a instruction node has the given opcode and constant.
 *
 * @param {object} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 * @param {number/string/object} constant The constant the instruction should have.
 */
function checkLdcInsn(instructionNode, opCode, constant) {
    return instructionNode.getOpcode() === opCode && instructionNode.cst === constant;
}

/**
 * Checks if a method instruction node has the given opcode, name, and descriptor.
 *
 * @param {object} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 * @param {string} name The name of the owning class the instruction should have.
 * @param {string} name The name the instruction should have.
 * @param {string} descriptor The descriptor the instruction should have.
 */
function checkMethodInsn(instructionNode, opCode, owner, name, descriptor) {
    return instructionNode.getOpcode() === opCode && instructionNode.owner === owner && instructionNode.name === name && instructionNode.desc === descriptor;
}

/**
 * Checks if a instruction node has the given opcode and index.
 *
 * @param {object} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 * @param {string} descriptor The descriptor that the instruction should have.
 */
function checkTypeInsn(instructionNode, opCode, descriptor) {
    return instructionNode.getOpcode() === opCode && instructionNode.desc === descriptor;
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
 * Gets this CoreMod's list of transformers.
 */
function initializeCoreMod() {
    return {
        /**
         * Adds an offset to the raycast that finds what block an entity is looking at.
         */
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

                        if (checkMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEyePosition", "(F)Lnet/minecraft/util/math/Vec3d;")) {
                            var newInstructions = new InsnList();

                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            newInstructions.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                "offsetRaycast",
                                "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                false
                            ));

                            // ...
                            // INVOKEVIRTUAL net/minecraft/entity/Entity.getEyePosition (F)Lnet/minecraft/util/math/Vec3d;
                            oldInstructions.insert(instruction, newInstructions);
                            // ...

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

        /**
         * Adds an offset to the raycast that finds what entity the player is looking at.
         */
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

                        if (checkMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEyePosition", "(F)Lnet/minecraft/util/math/Vec3d;")) {
                            var newInstructions = new InsnList();

                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            newInstructions.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                "offsetRaycast",
                                "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                false
                            ));

                            // ...
                            // INVOKEVIRTUAL net/minecraft/entity/Entity.getEyePosition (F)Lnet/minecraft/util/math/Vec3d;
                            oldInstructions.insert(instruction, newInstructions);
                            // ...

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

        /**
         * Prevents a player from rendering their first-person hand if they have an offset.
         */
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

        /**
         * Forces players to render their third-person model in first-person if they have an offset.
         */
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

                        if (checkMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/client/renderer/ActiveRenderInfo", "isThirdPerson", "()Z")) {
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

        /**
         * Offsets arrows shot by players.
         */
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

                        if (checkMethodInsn(instruction, Opcodes.INVOKESPECIAL, "net/minecraft/entity/projectile/AbstractArrowEntity", "<init>", "(Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V")) {
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

                            // ...
                            // INVOKESPECIAL net/minecraft/entity/projectile/AbstractArrowEntity.<init> (Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V
                            oldInstructions.insert(instruction, newInstructions);
                            // ...

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

        /**
         * Offsets throwables thrown by players.
         */
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

                        if (checkMethodInsn(instruction, Opcodes.INVOKESPECIAL, "net/minecraft/entity/projectile/ThrowableEntity", "<init>", "(Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V")) {
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

                            // ...
                            // INVOKESPECIAL net/minecraft/entity/projectile/ThrowableEntity.<init> (Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V
                            oldInstructions.insert(instruction, newInstructions);
                            // ...

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

        /**
         * Offsets eyes of ender launched by players.
         */
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

                        if (checkMethodInsn(instruction, Opcodes.INVOKESPECIAL, "net/minecraft/entity/item/EyeOfEnderEntity", "<init>", "(Lnet/minecraft/world/World;DDD)V")) {
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

                            // ...
                            // INVOKESPECIAL net/minecraft/entity/item/EyeOfEnderEntity.<init> (Lnet/minecraft/world/World;DDD)V
                            oldInstructions.insert(instruction, newInstructions);
                            // ...

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

        /**
         * Offsets cast fishing bobbers.
         */
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

                        if (checkMethodInsn(instruction, Opcodes.INVOKESPECIAL, "net/minecraft/entity/projectile/FishingBobberEntity", "<init>", "(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;II)V")) {
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

                            // ...
                            // INVOKESPECIAL net/minecraft/entity/projectile/FishingBobberEntity.<init> (Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;II)V
                            oldInstructions.insert(instruction, newInstructions);
                            // ...

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

        /**
         * Offsets the fishing line, and forces it to always render if it was in third-person.
         */
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
                    var successes = 0

                    // Forces third-person fishing line rendering.
                    for (var i = 0; i < oldInstructions.size() - 9; i++) {
                        var instruction = oldInstructions.get(i);

                        if (checkVarInsn(instruction, Opcodes.ALOAD, 0)) {
                            var instructions = [instruction];

                            for (var k = 1; k < 9; k++)
                                instructions.push(oldInstructions.get(i + k));

                            if (checkFieldInsn(instructions[1], Opcodes.GETFIELD, "net/minecraft/client/renderer/entity/FishRenderer", "renderManager", "Lnet/minecraft/client/renderer/entity/EntityRendererManager;") && checkFieldInsn(instructions[2], Opcodes.GETFIELD, "net/minecraft/client/renderer/entity/EntityRendererManager", "options", "Lnet/minecraft/client/GameSettings;") && checkInsn(instructions[3], Opcodes.IFNULL) &&
                                    checkVarInsn(instructions[4], Opcodes.ALOAD, 0) && checkFieldInsn(instructions[5], Opcodes.GETFIELD, "net/minecraft/client/renderer/entity/FishRenderer", "renderManager", "Lnet/minecraft/client/renderer/entity/EntityRendererManager;") && checkFieldInsn(instructions[6], Opcodes.GETFIELD, "net/minecraft/client/renderer/entity/EntityRendererManager", "options", "Lnet/minecraft/client/GameSettings;") && checkFieldInsn(instructions[7], Opcodes.GETFIELD, "net/minecraft/client/GameSettings", "thirdPersonView", "I") && checkInsn(instructions[8], Opcodes.IFGT)) {
                                oldInstructions.insertBefore(instructions[0], new JumpInsnNode(Opcodes.GOTO, instructions[8].label));

                                for (var k = 0; k < instructions.length; k++)
                                    oldInstructions.remove(instructions[k]);

                                successes |= 1;
                                break;
                            }
                        }
                    }

                    if (successes & 1 == 0)
                        logMessage("ERROR", "An error occurred while transforming render function in net.minecraft.client.renderer.FishRenderer:\n    Unable to find primary injection points");

                    // Puts in offset to fishing line position.
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


                                // ...
                                oldInstructions.insertBefore(instructions[0], getVectorList);
                                // DLOAD 25
                                // DLOAD 32
                                // DSUB
                                oldInstructions.insert(instructions[2], addXList);
                                // D2F
                                // FSTORE 38
                                // DLOAD 27
                                // DLOAD 34
                                // DSUB
                                oldInstructions.insert(instructions[7], addYList);
                                // D2F
                                // FLOAD 31
                                // FADD
                                // FSTORE 39
                                // DLOAD 29
                                // DLOAD 36
                                // DSUB
                                oldInstructions.insert(instructions[14], addZList);
                                // D2F
                                // FSTORE 40
                                // ...

                                successes |= 2;
                                break;
                            }
                        }
                    }

                    if (successes & 2 == 0) {
                        logMessage("ERROR", "An error occurred while transforming render function in net.minecraft.client.renderer.FishRenderer:\n    Unable to find secondary injection points");

                    } else if (successes & 1 == 1)
                        logMessage("INFO", "Successfully transformed render function in net.minecraft.client.renderer.FishRenderer");

                } catch (exception) {
                    logMessage("ERROR", "An error occurred while transforming render function in net.minecraft.client.renderer.FishRenderer:\n    " + exception);
                }

                return methodNode;
            }
        },

        /**
         * Corrects player-bobber distance calculation.
         * Offsets the destination of reeled-in entities and items.
         */
        "FishingBobberEntity": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.entity.projectile.FishingBobberEntity"
            },

            "transformer": function(classNode) {
                // Corrects player-bobber distance calculation.
                var shouldStopFishing = findMethodWithSignature(classNode, "shouldStopFishing", "()Z");

                if (shouldStopFishing != null) {
                    try {
                        var oldInstructions = shouldStopFishing.instructions;
                        var success = false;

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i);

                            if (checkMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getDistanceSq", "(Lnet/minecraft/entity/Entity;)D")) {
                                oldInstructions.insert(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/player/PlayerEntity;)D",
                                    false
                                ));
                                oldInstructions.remove(instruction);

                                success = true;
                                logMessage("INFO", "Successfully transformed shouldStopFishing function in net.minecraft.entity.projectile.FishingBobberEntity");

                                break;
                            }
                        }

                        if (!success)
                            logMessage("ERROR", "An error occurred while transforming shouldStopFishing function in net.minecraft.entity.projectile.FishingBobberEntity:\n    Unable to find injection point");

                    } catch (exception) {
                        logMessage("ERROR", "An error occurred while transforming shouldStopFishing function in net.minecraft.entity.projectile.FishingBobberEntity:\n    " + exception);
                    }

                } else
                    logMessage("ERROR", "An error occurred while transforming shouldStopFishing function in net.minecraft.entity.projectile.FishingBobberEntity:\n    Unable to find function to transform");


                // Offsets the destination of reeled-in items.
                var handleHookRetraction = findMethodWithSignature(classNode, "handleHookRetraction", "(Lnet/minecraft/item/ItemStack;)I");

                if (handleHookRetraction != null) {
                    try {
                        var oldInstructions = handleHookRetraction.instructions;
                        var success = false;

                        for (var i = 0; i < oldInstructions.size() - 25; i++) {
                            var instruction = oldInstructions.get(i);

                            if (checkVarInsn(instruction, Opcodes.ALOAD, 0)) {
                                var instructions = [instruction];

                                for (var k = 1; k < 25; k++) {
                                    var potentialInstruction = oldInstructions.get(i + k);

                                    if (potentialInstruction.getOpcode() !== -1)
                                        instructions.push(potentialInstruction);
                                }

                                if (checkFieldInsn(instructions[1], Opcodes.GETFIELD, "net/minecraft/entity/projectile/FishingBobberEntity", "angler", "Lnet/minecraft/entity/player/PlayerEntity;") && checkMethodInsn(instructions[2], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getPosX", "()D") && checkVarInsn(instructions[3], Opcodes.ALOAD, 0) && checkMethodInsn(instructions[4], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getPosX", "()D") && checkInsn(instructions[5], Opcodes.DSUB) && checkVarInsn(instructions[6], Opcodes.DSTORE, 10) &&
                                        checkVarInsn(instructions[7], Opcodes.ALOAD, 0) && checkFieldInsn(instructions[8], Opcodes.GETFIELD, "net/minecraft/entity/projectile/FishingBobberEntity", "angler", "Lnet/minecraft/entity/player/PlayerEntity;") && checkMethodInsn(instructions[9], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getPosY", "()D") && checkVarInsn(instructions[10], Opcodes.ALOAD, 0) && checkMethodInsn(instructions[11], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getPosY", "()D") && checkInsn(instructions[12], Opcodes.DSUB) && checkVarInsn(instructions[13], Opcodes.DSTORE, 12) &&
                                        checkVarInsn(instructions[14], Opcodes.ALOAD, 0) && checkFieldInsn(instructions[15], Opcodes.GETFIELD, "net/minecraft/entity/projectile/FishingBobberEntity", "angler", "Lnet/minecraft/entity/player/PlayerEntity;") && checkMethodInsn(instructions[16], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getPosZ", "()D") && checkVarInsn(instructions[17], Opcodes.ALOAD, 0) && checkMethodInsn(instructions[18], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getPosZ", "()D") && checkInsn(instructions[19], Opcodes.DSUB) && checkVarInsn(instructions[20], Opcodes.DSTORE, 14)) {
                                    var getVectorList = new InsnList();
                                    var addXList = new InsnList();
                                    var addYList = new InsnList();
                                    var addZList = new InsnList();


                                    getVectorList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    getVectorList.add(new FieldInsnNode(
                                        Opcodes.GETFIELD,
                                        "net/minecraft/entity/projectile/FishingBobberEntity",
                                        "angler",
                                        "Lnet/minecraft/entity/player/PlayerEntity;"
                                    ));
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


                                    // ...
                                    oldInstructions.insertBefore(instructions[0], getVectorList);
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/projectile/FishingBobberEntity.angler : Lnet/minecraft/entity/player/PlayerEntity;
                                    // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getPosX ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/projectile/FishingBobberEntity.getPosX ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[5], addXList);
                                    // DSTORE 10
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/projectile/FishingBobberEntity.angler : Lnet/minecraft/entity/player/PlayerEntity;
                                    // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getPosY ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/projectile/FishingBobberEntity.getPosY ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[12], addYList);
                                    // DSTORE 10
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/projectile/FishingBobberEntity.angler : Lnet/minecraft/entity/player/PlayerEntity;
                                    // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getPosZ ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/projectile/FishingBobberEntity.getPosZ ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[19], addZList);
                                    // DSTORE 14
                                    // ...

                                    logMessage("INFO", "Successfully transformed handleHookRetraction function in net.minecraft.entity.projectile.FishingBobberEntity");
                                    success = true;

                                    break;
                                }
                            }
                        }

                        if (!success)
                            logMessage("ERROR", "An error occurred while transforming handleHookRetraction function in net.minecraft.entity.projectile.FishingBobberEntity:\n    Unable to find injection points");

                    } catch (exception) {
                        logMessage("ERROR", "An error occurred while transforming handleHookRetraction function in net.minecraft.entity.projectile.FishingBobberEntity:\n    " + exception);
                    }

                } else
                    logMessage("ERROR", "An error occurred while transforming handleHookRetraction function in net.minecraft.entity.projectile.FishingBobberEntity:\n    Unable to find function to transform");


                // Offsets the destination of reeled-in entities and Entities.
                var bringInHookedEntity = findMethodWithSignature(classNode, "bringInHookedEntity", "()V");

                if (bringInHookedEntity != null) {
                    try {
                        var oldInstructions = bringInHookedEntity.instructions;
                        var success = false;

                        for (var i = 0; i < oldInstructions.size() - 18; i++) {
                            var instruction = oldInstructions.get(i);

                            if (checkVarInsn(instruction, Opcodes.ALOAD, 0)) {
                                var instructions = [instruction];

                                for (var k = 1; k < 18; k++)
                                    instructions.push(oldInstructions.get(i + k));

                                if (checkFieldInsn(instructions[1], Opcodes.GETFIELD, "net/minecraft/entity/projectile/FishingBobberEntity", "angler", "Lnet/minecraft/entity/player/PlayerEntity;") && checkMethodInsn(instructions[2], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getPosX", "()D") && checkVarInsn(instructions[3], Opcodes.ALOAD, 0) && checkMethodInsn(instructions[4], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getPosX", "()D") && checkInsn(instructions[5], Opcodes.DSUB) &&
                                        checkVarInsn(instructions[6], Opcodes.ALOAD, 0) && checkFieldInsn(instructions[7], Opcodes.GETFIELD, "net/minecraft/entity/projectile/FishingBobberEntity", "angler", "Lnet/minecraft/entity/player/PlayerEntity;") && checkMethodInsn(instructions[8], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getPosY", "()D") && checkVarInsn(instructions[9], Opcodes.ALOAD, 0) && checkMethodInsn(instructions[10], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getPosY", "()D") && checkInsn(instructions[11], Opcodes.DSUB) &&
                                        checkVarInsn(instructions[12], Opcodes.ALOAD, 0) && checkFieldInsn(instructions[13], Opcodes.GETFIELD, "net/minecraft/entity/projectile/FishingBobberEntity", "angler", "Lnet/minecraft/entity/player/PlayerEntity;") && checkMethodInsn(instructions[14], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getPosZ", "()D") && checkVarInsn(instructions[15], Opcodes.ALOAD, 0) && checkMethodInsn(instructions[16], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getPosZ", "()D") && checkInsn(instructions[17], Opcodes.DSUB)) {
                                    var addXList = new InsnList();
                                    var addYList = new InsnList();
                                    var addZList = new InsnList();


                                    addXList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    addXList.add(new FieldInsnNode(
                                        Opcodes.GETFIELD,
                                        "net/minecraft/entity/projectile/FishingBobberEntity",
                                        "angler",
                                        "Lnet/minecraft/entity/player/PlayerEntity;"
                                    ));
                                    addXList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                        "getOffsets",
                                        "(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/math/Vec3d;",
                                        false
                                    ));
                                    addXList.add(new FieldInsnNode(
                                        Opcodes.GETFIELD,
                                        "net/minecraft/util/math/Vec3d",
                                        "x",
                                        "D"
                                    ));
                                    addXList.add(new InsnNode(Opcodes.DADD));

                                    addYList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    addYList.add(new FieldInsnNode(
                                        Opcodes.GETFIELD,
                                        "net/minecraft/entity/projectile/FishingBobberEntity",
                                        "angler",
                                        "Lnet/minecraft/entity/player/PlayerEntity;"
                                    ));
                                    addYList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                        "getOffsets",
                                        "(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/math/Vec3d;",
                                        false
                                    ));
                                    addYList.add(new FieldInsnNode(
                                        Opcodes.GETFIELD,
                                        "net/minecraft/util/math/Vec3d",
                                        "y",
                                        "D"
                                    ));
                                    addYList.add(new InsnNode(Opcodes.DADD));

                                    addZList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    addZList.add(new FieldInsnNode(
                                        Opcodes.GETFIELD,
                                        "net/minecraft/entity/projectile/FishingBobberEntity",
                                        "angler",
                                        "Lnet/minecraft/entity/player/PlayerEntity;"
                                    ));
                                    addZList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                        "getOffsets",
                                        "(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/math/Vec3d;",
                                        false
                                    ));
                                    addZList.add(new FieldInsnNode(
                                        Opcodes.GETFIELD,
                                        "net/minecraft/util/math/Vec3d",
                                        "z",
                                        "D"
                                    ));
                                    addZList.add(new InsnNode(Opcodes.DADD));


                                    // ...
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/projectile/FishingBobberEntity.angler : Lnet/minecraft/entity/player/PlayerEntity;
                                    // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getPosX ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/projectile/FishingBobberEntity.getPosX ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[5], addXList);
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/projectile/FishingBobberEntity.angler : Lnet/minecraft/entity/player/PlayerEntity;
                                    // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getPosY ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/projectile/FishingBobberEntity.getPosY ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[11], addYList);
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/projectile/FishingBobberEntity.angler : Lnet/minecraft/entity/player/PlayerEntity;
                                    // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getPosZ ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/projectile/FishingBobberEntity.getPosZ ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[17], addZList);
                                    // ...

                                    logMessage("INFO", "Successfully transformed bringInHookedEntity function in net.minecraft.entity.projectile.FishingBobberEntity");
                                    success = true;

                                    break;
                                }
                            }
                        }

                        if (!success)
                            logMessage("ERROR", "An error occurred while transforming bringInHookedEntity function in net.minecraft.entity.projectile.FishingBobberEntity:\n    Unable to find injection points");

                    } catch (exception) {
                        logMessage("ERROR", "An error occurred while transforming bringInHookedEntity function in net.minecraft.entity.projectile.FishingBobberEntity:\n    " + exception);
                    }

                } else
                    logMessage("ERROR", "An error occurred while transforming bringInHookedEntity function in net.minecraft.entity.projectile.FishingBobberEntity:\n    Unable to find function to transform");

                return classNode;
            }
        },

        /**
         * Allows players to break far away blocks.
         */
        "PlayerInteractionManager": {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.server.management.PlayerInteractionManager",
                "methodName": "func_225416_a",
                "methodDesc": "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/network/play/client/CPlayerDiggingPacket$Action;Lnet/minecraft/util/Direction;I)V"
            },

            "transformer": function(methodNode) {
                try {
                    var oldInstructions = methodNode.instructions;
                    var success = false;

                    // Puts in offset to fishing line position.
                    for (var i = 0; i < oldInstructions.size() - 38; i++) {
                        var instruction = oldInstructions.get(i);

                        if (checkVarInsn(instruction, Opcodes.ALOAD, 0)) {
                            var instructions = [instruction];

                            for (var k = 1; k < 38; k++) {
                                var potentialInstruction = oldInstructions.get(i + k);

                                if (potentialInstruction.getOpcode() !== -1)
                                    instructions.push(potentialInstruction);
                            }

                            if (checkFieldInsn(instructions[1], Opcodes.GETFIELD, "net/minecraft/server/management/PlayerInteractionManager", "player", "Lnet/minecraft/entity/player/ServerPlayerEntity;") && checkMethodInsn(instructions[2], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/ServerPlayerEntity", "getPosX", "()D") && checkVarInsn(instructions[3], Opcodes.ALOAD, 1) && checkMethodInsn(instructions[4], Opcodes.INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", "getX", "()I") && checkInsn(instructions[5], Opcodes.I2D) && checkLdcInsn(instructions[6], Opcodes.LDC, 0.5) && checkInsn(instructions[7], Opcodes.DADD) && checkInsn(instructions[8], Opcodes.DSUB) && checkVarInsn(instructions[9], Opcodes.DSTORE, 5) &&
                                    checkVarInsn(instructions[10], Opcodes.ALOAD, 0) && checkFieldInsn(instructions[11], Opcodes.GETFIELD, "net/minecraft/server/management/PlayerInteractionManager", "player", "Lnet/minecraft/entity/player/ServerPlayerEntity;") && checkMethodInsn(instructions[12], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/ServerPlayerEntity", "getPosY", "()D") && checkVarInsn(instructions[13], Opcodes.ALOAD, 1) && checkMethodInsn(instructions[14], Opcodes.INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", "getY", "()I") && checkInsn(instructions[15], Opcodes.I2D) && checkLdcInsn(instructions[16], Opcodes.LDC, 0.5) && checkInsn(instructions[17], Opcodes.DADD) && checkInsn(instructions[18], Opcodes.DSUB) && checkLdcInsn(instructions[19], Opcodes.LDC, 1.5) && checkInsn(instructions[20], Opcodes.DADD) && checkVarInsn(instructions[21], Opcodes.DSTORE, 7) &&
                                    checkVarInsn(instructions[22], Opcodes.ALOAD, 0) && checkFieldInsn(instructions[23], Opcodes.GETFIELD, "net/minecraft/server/management/PlayerInteractionManager", "player", "Lnet/minecraft/entity/player/ServerPlayerEntity;") && checkMethodInsn(instructions[24], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/ServerPlayerEntity", "getPosZ", "()D") && checkVarInsn(instructions[25], Opcodes.ALOAD, 1) && checkMethodInsn(instructions[26], Opcodes.INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", "getZ", "()I") && checkInsn(instructions[27], Opcodes.I2D) && checkLdcInsn(instructions[28], Opcodes.LDC, 0.5) && checkInsn(instructions[29], Opcodes.DADD) && checkInsn(instructions[30], Opcodes.DSUB) && checkVarInsn(instructions[31], Opcodes.DSTORE, 9)) {
                                var getVectorList = new InsnList();
                                var addXList = new InsnList();
                                var addYList = new InsnList();
                                var addZList = new InsnList();


                                getVectorList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                getVectorList.add(new FieldInsnNode(
                                    Opcodes.GETFIELD,
                                    "net/minecraft/server/management/PlayerInteractionManager",
                                    "player",
                                    "Lnet/minecraft/entity/player/ServerPlayerEntity;"
                                ));
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


                                // ...
                                oldInstructions.insertBefore(instructions[0], getVectorList);
                                // ALOAD 0
                                // GETFIELD net/minecraft/server/management/PlayerInteractionManager.player : Lnet/minecraft/entity/player/ServerPlayerEntity;
                                //INVOKEVIRTUAL net/minecraft/entity/player/ServerPlayerEntity.getPosX ()D
                                // ALOAD 1
                                // INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getX ()I
                                // I2D
                                // LDC 0.5
                                // DADD
                                // DSUB
                                oldInstructions.insert(instructions[8], addXList);
                                // DSTORE 5
                                // ALOAD 0
                                // GETFIELD net/minecraft/server/management/PlayerInteractionManager.player : Lnet/minecraft/entity/player/ServerPlayerEntity;
                                // INVOKEVIRTUAL net/minecraft/entity/player/ServerPlayerEntity.getPosY ()D
                                // ALOAD 1
                                // INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getY ()I
                                // I2D
                                // LDC 0.5
                                // DADD
                                // DSUB
                                // LDC 1.5
                                // DADD
                                oldInstructions.insert(instructions[20], addYList);
                                // DSTORE 7
                                // ALOAD 0
                                // GETFIELD net/minecraft/server/management/PlayerInteractionManager.player : Lnet/minecraft/entity/player/ServerPlayerEntity;
                                // INVOKEVIRTUAL net/minecraft/entity/player/ServerPlayerEntity.getPosZ ()D
                                // ALOAD 1
                                // INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getZ ()I
                                // I2D
                                // LDC 0.5
                                // DADD
                                // DSUB
                                oldInstructions.insert(instructions[30], addZList);
                                // DSTORE 9
                                // ...

                                logMessage("INFO", "Successfully transformed func_225416_a function in net.minecraft.server.management.PlayerInteractionManager");
                                success = true;

                                break;
                            }
                        }
                    }

                    if (!success)
                        logMessage("ERROR", "An error occurred while transforming func_225416_a function in net.minecraft.server.management.PlayerInteractionManager:\n    Unable to find injection points");

                } catch (exception) {
                    logMessage("ERROR", "An error occurred while transforming func_225416_a function in net.minecraft.server.management.PlayerInteractionManager:\n    " + exception);
                }

                return methodNode;
            }
        },

        /**
         * Modifies the logic of leashes.
         */
        "CreatureEntity": {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.entity.CreatureEntity",
                "methodName": "updateLeashedState",
                "methodDesc": "()V"
            },

            "transformer": function(methodNode) {
                try {
                    var oldInstructions = methodNode.instructions;
                    var successes = 0;

                    // Modifies home position set for leashes.
                    for (var i = 0; i < oldInstructions.size() - 4; i++) {
                        var instruction = oldInstructions.get(i);

                        if (checkTypeInsn(instruction, Opcodes.NEW, "net/minecraft/util/math/BlockPos")) {
                            var instructions = [instruction];

                            for (var k = 1; k < 4; k++)
                                instructions.push(oldInstructions.get(i + k));

                            if (checkInsn(instructions[1], Opcodes.DUP) && checkVarInsn(instructions[2], Opcodes.ALOAD, 1) && checkMethodInsn(instructions[3], Opcodes.INVOKESPECIAL, "net/minecraft/util/math/BlockPos", "<init>", "(Lnet/minecraft/entity/Entity;)V")) {
                                var newInstructions = new InsnList();

                                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                                newInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "modifiedBlockPos",
                                    "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/BlockPos;"
                                ));

                                oldInstructions.insertBefore(instructions[0], newInstructions);

                                for (var k = 0; k < instructions.length; k++)
                                    oldInstructions.remove(instructions[k]);


                                successes |= 1;
                                break;
                            }
                        }
                    }

                    if (successes & 1 == 0)
                        logMessage("ERROR", "An error occurred while transforming updateLeashedState function in net.minecraft.entity.CreatureEntity:\n    Unable to find primary injection points");


                    // Modifies distance calculation for leashes.
                    for (var i = 0; i < oldInstructions.size(); i++) {
                        var instruction = oldInstructions.get(i);

                        if (checkMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getDistance", "(Lnet/minecraft/entity/Entity;)F")) {
                            oldInstructions.insert(instruction, new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                "modifiedGetDistance",
                                "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;)F",
                                false
                            ));
                            oldInstructions.remove(instruction);

                            successes |= 2;
                            break;
                        }
                    }

                    if (successes & 2 == 0)
                        logMessage("ERROR", "An error occurred while transforming updateLeashedState function in net.minecraft.entity.CreatureEntity:\n    Unable to find secondary injection points");


                    // Modifies pull position for leashes.
                    for (var i = 0; i < oldInstructions.size() - 31; i++) {
                        var instruction = oldInstructions.get(i);

                        if (checkVarInsn(instruction, Opcodes.ALOAD, 1)) {
                            var instructions = [instruction];

                            for (var k = 1; k < 31; k++) {
                                var potentialInstruction = oldInstructions.get(i + k);

                                if (potentialInstruction.getOpcode() !== -1)
                                    instructions.push(potentialInstruction);
                            }

                            if (checkMethodInsn(instructions[1], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosX", "()D") && checkVarInsn(instructions[2], Opcodes.ALOAD, 0) && checkMethodInsn(instructions[3], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getPosX", "()D") && checkInsn(instructions[4], Opcodes.DSUB) && checkVarInsn(instructions[5], Opcodes.FLOAD, 2) && checkInsn(instructions[6], Opcodes.F2D) && checkInsn(instructions[7], Opcodes.DDIV) && checkVarInsn(instructions[8], Opcodes.DSTORE, 3) &&
                                    checkVarInsn(instructions[9], Opcodes.ALOAD, 1) && checkMethodInsn(instructions[10], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosY", "()D") && checkVarInsn(instructions[11], Opcodes.ALOAD, 0) && checkMethodInsn(instructions[12], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getPosY", "()D") && checkInsn(instructions[13], Opcodes.DSUB) && checkVarInsn(instructions[14], Opcodes.FLOAD, 2) && checkInsn(instructions[15], Opcodes.F2D) && checkInsn(instructions[16], Opcodes.DDIV) && checkVarInsn(instructions[17], Opcodes.DSTORE, 5) &&
                                    checkVarInsn(instructions[18], Opcodes.ALOAD, 1) && checkMethodInsn(instructions[19], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosZ", "()D") && checkVarInsn(instructions[20], Opcodes.ALOAD, 0) && checkMethodInsn(instructions[21], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getPosZ", "()D") && checkInsn(instructions[22], Opcodes.DSUB) && checkVarInsn(instructions[23], Opcodes.FLOAD, 2) && checkInsn(instructions[24], Opcodes.F2D) && checkInsn(instructions[25], Opcodes.DDIV) && checkVarInsn(instructions[26], Opcodes.DSTORE, 7)) {
                                var getVectorList = new InsnList();
                                var addXList = new InsnList();
                                var addYList = new InsnList();
                                var addZList = new InsnList();


                                getVectorList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                                getVectorList.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "getOffsets",
                                    "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
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


                                // ...
                                oldInstructions.insertBefore(instructions[0], getVectorList);
                                // ALOAD 1
                                // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosX ()D
                                // ALOAD 0
                                // INVOKEVIRTUAL net/minecraft/entity/CreatureEntity.getPosX ()D
                                // DSUB
                                oldInstructions.insert(instructions[4], addXList);
                                // FLOAD 2
                                // F2D
                                // DDIV
                                // DSTORE 3
                                // ALOAD 1
                                // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosY ()D
                                // ALOAD 0
                                // INVOKEVIRTUAL net/minecraft/entity/CreatureEntity.getPosY ()D
                                // DSUB
                                oldInstructions.insert(instructions[13], addYList);
                                // FLOAD 2
                                // F2D
                                // DDIV
                                // DSTORE 5
                                // ALOAD 1
                                // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosZ ()D
                                // ALOAD 0
                                // INVOKEVIRTUAL net/minecraft/entity/CreatureEntity.getPosZ ()D
                                // DSUB
                                oldInstructions.insert(instructions[22], addZList);
                                // FLOAD 2
                                // F2D
                                // DDIV
                                // DSTORE 7
                                // ...

                                successes |= 4;
                                break;
                            }
                        }
                    }

                    if (successes & 4 == 0)
                        logMessage("ERROR", "An error occurred while transforming updateLeashedState function in net.minecraft.entity.CreatureEntity:\n    Unable to find tertiary injection points");


                    // Modifies AI move behavior when leashed.
                    for (var i = 0; i < oldInstructions.size() - 27; i++) {
                        var instruction = oldInstructions.get(i);

                        if (checkTypeInsn(instruction, Opcodes.NEW, "net/minecraft/util/math/Vec3d")) {
                            var instructions = [instruction];

                            for (var k = 1; k < 27; k++)
                                instructions.push(oldInstructions.get(i + k));

                            if (checkInsn(instructions[1], Opcodes.DUP) &&
                                    checkVarInsn(instructions[2], Opcodes.ALOAD, 1) && checkMethodInsn(instructions[3], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosX", "()D") && checkVarInsn(instructions[4], Opcodes.ALOAD, 0) && checkMethodInsn(instructions[5], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getPosX", "()D") && checkInsn(instructions[6], Opcodes.DSUB) &&
                                    checkVarInsn(instructions[7], Opcodes.ALOAD, 1) && checkMethodInsn(instructions[8], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosY", "()D") && checkVarInsn(instructions[9], Opcodes.ALOAD, 0) && checkMethodInsn(instructions[10], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getPosY", "()D") && checkInsn(instructions[11], Opcodes.DSUB) &&
                                    checkVarInsn(instructions[12], Opcodes.ALOAD, 1) && checkMethodInsn(instructions[13], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosZ", "()D") && checkVarInsn(instructions[14], Opcodes.ALOAD, 0) && checkMethodInsn(instructions[15], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getPosZ", "()D") && checkInsn(instructions[16], Opcodes.DSUB) &&
                                    checkMethodInsn(instructions[17], Opcodes.INVOKESPECIAL, "net/minecraft/util/math/Vec3d", "<init>", "(DDD)V") && checkMethodInsn(instructions[18], Opcodes.INVOKEVIRTUAL, "net/minecraft/util/math/Vec3d", "normalize", "()Lnet/minecraft/util/math/Vec3d;") && checkVarInsn(instructions[19], Opcodes.FLOAD, 2) && checkInsn(instructions[20], Opcodes.FCONST_2) && checkInsn(instructions[21], Opcodes.FSUB) && checkInsn(instructions[22], Opcodes.FCONST_0) && checkMethodInsn(instructions[23], Opcodes.INVOKESTATIC, "java/lang/Math", "max", "(FF)F") && checkInsn(instructions[24], Opcodes.F2D) && checkMethodInsn(instructions[25], Opcodes.INVOKEVIRTUAL, "net/minecraft/util/math/Vec3d", "scale", "(D)Lnet/minecraft/util/math/Vec3d;") && checkVarInsn(instructions[26], Opcodes.ASTORE, 4)) {
                                var addXList = new InsnList();
                                var addYList = new InsnList();
                                var addZList = new InsnList();


                                addXList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                                addXList.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "getOffsets",
                                    "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ));
                                addXList.add(new FieldInsnNode(
                                    Opcodes.GETFIELD,
                                    "net/minecraft/util/math/Vec3d",
                                    "x",
                                    "D"
                                ));
                                addXList.add(new InsnNode(Opcodes.DADD));

                                addYList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                                addYList.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "getOffsets",
                                    "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ));
                                addYList.add(new FieldInsnNode(
                                    Opcodes.GETFIELD,
                                    "net/minecraft/util/math/Vec3d",
                                    "y",
                                    "D"
                                ));
                                addYList.add(new InsnNode(Opcodes.DADD));

                                addZList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                                addZList.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "getOffsets",
                                    "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ));
                                addZList.add(new FieldInsnNode(
                                    Opcodes.GETFIELD,
                                    "net/minecraft/util/math/Vec3d",
                                    "z",
                                    "D"
                                ));
                                addZList.add(new InsnNode(Opcodes.DADD));


                                // ...
                                // NEW net/minecraft/util/math/Vec3d
                                // DUP
                                // ALOAD 1
                                // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosX ()D
                                oldInstructions.insert(instructions[3], addXList);
                                // ALOAD 0
                                // INVOKEVIRTUAL net/minecraft/entity/CreatureEntity.getPosX ()D
                                // DSUB
                                // ALOAD 1
                                // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosY ()D
                                oldInstructions.insert(instructions[8], addYList);
                                // ALOAD 0
                                // INVOKEVIRTUAL net/minecraft/entity/CreatureEntity.getPosY ()D
                                // DSUB
                                // ALOAD 1
                                // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosZ ()D
                                oldInstructions.insert(instructions[13], addZList);
                                // ALOAD 0
                                // INVOKEVIRTUAL net/minecraft/entity/CreatureEntity.getPosZ ()D
                                // DSUB
                                // INVOKESPECIAL net/minecraft/util/math/Vec3d.<init> (DDD)V
                                // INVOKEVIRTUAL net/minecraft/util/math/Vec3d.normalize ()Lnet/minecraft/util/math/Vec3d;
                                // FLOAD 2
                                // FCONST_2
                                // FSUB
                                // FCONST_0
                                // INVOKESTATIC java/lang/Math.max (FF)F
                                // F2D
                                // INVOKEVIRTUAL net/minecraft/util/math/Vec3d.scale (D)Lnet/minecraft/util/math/Vec3d;
                                // ASTORE 4
                                // ...

                                successes |= 8;
                                break;
                            }
                        }
                    }

                    if (successes & 8 == 0) {
                        logMessage("ERROR", "An error occurred while transforming updateLeashedState function in net.minecraft.entity.CreatureEntity:\n    Unable to find the fourth set of injection points");

                    } else if (successes ^ 15 == 0)
                        logMessage("INFO", "Successfully transformed updateLeashedState function in net.minecraft.entity.CreatureEntity");

                } catch (exception) {
                    logMessage("ERROR", "An error occurred while transforming updateLeashedState function in net.minecraft.entity.CreatureEntity:\n    " + exception);
                }

                return methodNode;
            }
        },

        /**
         * Modifies the rendering of leashes.
         */
        "MobRenderer": {
            "target": {
                "type": "METHOD",
                "class": "net.minecraft.client.renderer.entity.MobRenderer",
                "methodName": "renderLeash",
                "methodDesc": "(Lnet/minecraft/entity/MobEntity;FLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;Lnet/minecraft/entity/Entity;)V"
            },

            "transformer": function(methodNode) {
                try {
                    var oldInstructions = methodNode.instructions;
                    var successes = 0;
                    var vectorIndex = methodNode.maxLocals;

                    // Modifies the x-component of the leash render position.
                    for (var i = 0; i < oldInstructions.size() - 7; i++) {
                        var instruction = oldInstructions.get(i);

                        if (checkVarInsn(instruction, Opcodes.FLOAD, 2)) {
                            var instructions = [instruction];

                            for (var k = 1; k < 7; k++)
                                instructions.push(oldInstructions.get(i + k));

                            if (checkInsn(instructions[1], Opcodes.F2D) && checkVarInsn(instructions[2], Opcodes.ALOAD, 5) && checkFieldInsn(instructions[3], Opcodes.GETFIELD, "net/minecraft/entity/Entity", "prevPosX", "D") && checkVarInsn(instructions[4], Opcodes.ALOAD, 5) && checkMethodInsn(instructions[5], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosX", "()D") && checkMethodInsn(instructions[6], Opcodes.INVOKESTATIC, "net/minecraft/util/math/MathHelper", "lerp", "(DDD)D")) {
                                var getVectorList = new InsnList();
                                var addXList = new InsnList();


                                getVectorList.add(new VarInsnNode(Opcodes.ALOAD, 5));
                                getVectorList.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "getOffsets",
                                    "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ));
                                getVectorList.add(new VarInsnNode(Opcodes.ASTORE, vectorIndex));

                                addXList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex));
                                addXList.add(new FieldInsnNode(
                                    Opcodes.GETFIELD,
                                    "net/minecraft/util/math/Vec3d",
                                    "x",
                                    "D"
                                ));
                                addXList.add(new InsnNode(Opcodes.DADD));


                                // ...
                                oldInstructions.insertBefore(instructions[0], getVectorList);
                                // FLOAD 2
                                // F2D
                                // ALOAD 5
                                // GETFIELD net/minecraft/entity/Entity.prevPosX : D
                                // ALOAD 5
                                // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosX ()D
                                // INVOKESTATIC net/minecraft/util/math/MathHelper.lerp (DDD)D
                                oldInstructions.insert(instructions[6], addXList);
                                // ...

                                successes |= 1;
                                break;
                            }
                        }
                    }

                    if (successes & 1 == 0) {
                        logMessage("ERROR", "An error occurred while transforming renderLeash function in net.minecraft.client.renderer.entity.MobRenderer:\n    Unable to find primary injection points");

                    } else {
                        // Modifies the y-component of the leash render position.
                        for (var i = 0; i < oldInstructions.size() - 15; i++) {
                            var instruction = oldInstructions.get(i);

                            if (checkVarInsn(instruction, Opcodes.FLOAD, 2)) {
                                var instructions = [instruction];

                                for (var k = 1; k < 15; k++)
                                    instructions.push(oldInstructions.get(i + k));

                                if (checkInsn(instructions[1], Opcodes.F2D) && checkVarInsn(instructions[2], Opcodes.ALOAD, 5) && checkFieldInsn(instructions[3], Opcodes.GETFIELD, "net/minecraft/entity/Entity", "prevPosY", "D") && checkVarInsn(instructions[4], Opcodes.ALOAD, 5) && checkMethodInsn(instructions[5], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEyeHeight", "()F") && checkInsn(instructions[6], Opcodes.F2D) && checkLdcInsn(instructions[7], Opcodes.LDC, 0.7) && checkInsn(instructions[8], Opcodes.DMUL) && checkInsn(instructions[9], Opcodes.DADD) &&
                                        checkVarInsn(instructions[10], Opcodes.ALOAD, 5) && checkMethodInsn(instructions[11], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosY", "()D") && checkVarInsn(instructions[12], Opcodes.ALOAD, 5) && checkMethodInsn(instructions[13], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEyeHeight", "()F") && checkInsn(instructions[14], Opcodes.F2D)) {
                                    var addYList = new InsnList();

                                    addYList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex))
                                    addYList.add(new FieldInsnNode(
                                        Opcodes.GETFIELD,
                                        "net/minecraft/util/math/Vec3d",
                                        "y",
                                        "D"
                                    ));
                                    addYList.add(new InsnNode(Opcodes.DADD));

                                    // ...
                                    // FLOAD 2
                                    // F2D
                                    // ALOAD 5
                                    // GETFIELD net/minecraft/entity/Entity.prevPosY : D
                                    // ALOAD 5
                                    // INVOKEVIRTUAL net/minecraft/entity/Entity.getEyeHeight ()F
                                    // F2D
                                    // LDC 0.7
                                    // DMUL
                                    // DADD
                                    // ALOAD 5
                                    // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosY ()D
                                    // ALOAD 5
                                    // INVOKEVIRTUAL net/minecraft/entity/Entity.getEyeHeight ()F
                                    // F2D
                                    oldInstructions.insert(instructions[14], addYList);
                                    // ...

                                    successes |= 2;
                                    break;
                                }
                            }
                        }

                        if (successes & 2 == 0)
                            logMessage("ERROR", "An error occurred while transforming renderLeash function in net.minecraft.client.renderer.entity.MobRenderer:\n    Unable to find secondary injection points");


                        // Modifies the z-component of the leash render position.
                        for (var i = 0; i < oldInstructions.size() - 7; i++) {
                            var instruction = oldInstructions.get(i);

                            if (checkVarInsn(instruction, Opcodes.FLOAD, 2)) {
                                var instructions = [instruction];

                                for (var k = 1; k < 7; k++)
                                    instructions.push(oldInstructions.get(i + k));

                                if (checkInsn(instructions[1], Opcodes.F2D) && checkVarInsn(instructions[2], Opcodes.ALOAD, 5) && checkFieldInsn(instructions[3], Opcodes.GETFIELD, "net/minecraft/entity/Entity", "prevPosZ", "D") && checkVarInsn(instructions[4], Opcodes.ALOAD, 5) && checkMethodInsn(instructions[5], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosZ", "()D") && checkMethodInsn(instructions[6], Opcodes.INVOKESTATIC, "net/minecraft/util/math/MathHelper", "lerp", "(DDD)D")) {
                                    var addZList = new InsnList();

                                    addZList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex));
                                    addZList.add(new FieldInsnNode(
                                        Opcodes.GETFIELD,
                                        "net/minecraft/util/math/Vec3d",
                                        "z",
                                        "D"
                                    ));
                                    addZList.add(new InsnNode(Opcodes.DADD));

                                    // ...
                                    // FLOAD 2
                                    // F2D
                                    // ALOAD 5
                                    // GETFIELD net/minecraft/entity/Entity.prevPosZ : D
                                    // ALOAD 5
                                    // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosZ ()D
                                    // INVOKESTATIC net/minecraft/util/math/MathHelper.lerp (DDD)D
                                    oldInstructions.insert(instructions[6], addZList);
                                    // ...

                                    successes |= 4;
                                    break;
                                }
                            }
                        }

                        if (successes & 4 == 0) {
                            logMessage("ERROR", "An error occurred while transforming renderLeash function in net.minecraft.client.renderer.entity.MobRenderer:\n    Unable to find tertiary injection points");

                        } else if (successes ^ 7 == 0)
                            logMessage("INFO", "Successfully transformed renderLeash function in net.minecraft.client.renderer.entity.MobRenderer");
                    }
                } catch (exception) {
                    logMessage("ERROR", "An error occurred while transforming renderLeash function in net.minecraft.client.renderer.entity.MobRenderer:\n    " + exception);
                }

                return methodNode;
            }
        }
    }
}