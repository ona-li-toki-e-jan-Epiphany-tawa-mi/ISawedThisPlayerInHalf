var Opcodes = Java.type("org.objectweb.asm.Opcodes")
var InsnList = Java.type("org.objectweb.asm.tree.InsnList")
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode")
var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode")
var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode")
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode")
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode")



/**
 * Forces the game to account for the player's offsets in it's calculations.
 */
// TODO Find a way to make a copy of method and class nodes so that when transforms fail the unmodified one can be returned.
// TODO Find an automatic way to minify this file when making a jar.
// TODO Move strings into variables.



/**
 * Tries to find a method within the given class.
 * Returns null if nothing is found.
 *
 * @param {object/ClassNode} classNode The class node to search through.
 * @param {string} methodName The name of the method to find.
 * @param {string} descriptor The descriptor of the method to find.
 *
 * @returns {null} The found method. Or null, if nothing is found.
 */
function findMethodWithSignature(classNode, methodName, descriptor) {
    for (var i in classNode.methods) {
        var method = classNode.methods[i]

        if (method.name === methodName && method.desc === descriptor)
            return method
    }

    return null
}

/**
 * Tries to find a method within the given class.
 * Returns null if nothing is found.
 *
 * @param {object/ClassNode} classNode The class node to search through.
 * @param {string} methodName The name of the method to find.
 * @param {string} methodName The obfuscated name of the method to find.
 * @param {string} descriptor The descriptor of the method to find.
 *
 * @returns {null} The found method. Or null, if nothing is found.
 */
function findObfuscatedMethodWithSignature(classNode, methodName, obfuscatedName, descriptor) {
    for (var i in classNode.methods) {
        var method = classNode.methods[i]

        if ((method.name === methodName || method.name === obfuscatedName) && method.desc === descriptor)
            return method
    }

    return null
}


/**
 * Checks if a field instruction node has the given opcode, owner, name, and descriptor.
 *
 * @param {object/FieldNode} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 * @param {string} name The owner the instruction should have.
 * @param {string} name The name the instruction should have.
 * @param {string} obfuscatedName The obfuscated name the instruction should have.
 * @param {string} descriptor The descriptor the instruction should have.
 */
function checkObfuscatedFieldInsn(instructionNode, opCode, owner, name, obfuscatedName, descriptor) {
    return instructionNode.getOpcode() === opCode && instructionNode.owner === owner && (instructionNode.name === name ||
        instructionNode.name === obfuscatedName) && instructionNode.desc === descriptor
}

/**
 * Checks if an instruction node has the given opcode.
 *
 * @param {object/InsnNode} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 */
function checkInsn(instructionNode, opCode) {
    return instructionNode.getOpcode() === opCode
}

/**
 * Checks if a jump instruction node has the given opcode.
 *
 * @param {object/JumpInsnNode} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 */
function checkJumpInsn(instructionNode, opCode) {
    return checkInsn(instructionNode, opCode)
}

/**
 * Checks if a load constant instruction node has the given opcode and constant.
 *
 * @param {object/LdcInsnNode} instructionNode The instruction node to check.
 * @param constant The constant the instruction should have.
 */
function checkLdcInsn(instructionNode, constant) {
    return instructionNode.getOpcode() === Opcodes.LDC && instructionNode.cst === constant
}

/**
 * Checks if a method instruction node has the given opcode, name, and descriptor.
 *
 * @param {object/MethodInsnNode} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 * @param {string} name The name of the owning class the instruction should have.
 * @param {string} name The name the instruction should have.
 * @param {string} descriptor The descriptor the instruction should have.
 */
function checkMethodInsn(instructionNode, opCode, owner, name, descriptor) {
    return instructionNode.getOpcode() === opCode && instructionNode.owner === owner && instructionNode.name === name &&
        instructionNode.desc === descriptor
}

/**
 * Checks if a method instruction node has the given opcode, name, and descriptor.
 *
 * @param {object/MethodNode} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 * @param {string} owner The name of the owning class the instruction should have.
 * @param {string} name The name the instruction should have.
 * @param {string} obfuscatedName The name the instruction should have.
 * @param {string} descriptor The descriptor the instruction should have.
 */
function checkObfuscatedMethodInsn(instructionNode, opCode, owner, name, obfuscatedName, descriptor) {
    return instructionNode.getOpcode() === opCode && instructionNode.owner === owner && (instructionNode.name === name ||
        instructionNode.name === obfuscatedName) && instructionNode.desc === descriptor
}

/**
 * Checks if a var instruction node has the given opcode and index.
 *
 * @param {object/VarInsnNode} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 * @param {number} varIndex The index of the constant pool that the instruction should have.
 */
function checkVarInsn(instructionNode, opCode, varIndex) {
    return instructionNode.getOpcode() === opCode && instructionNode.var === varIndex
}



var LoggingLevel = {
    DEBUG: {numericLevel: 0, name: "DEBUG"},
    ERROR: {numericLevel: 1, name: "ERROR"}
}
// The minimum logging level required for a message to be logged.
var minimumLoggingLevel = LoggingLevel.DEBUG

/**
 * Logs a message to the console, showing the time and severity level with it.
 *
 * @param {enum/LoggingLevel} loggingLevel The severity level of the message (e.x. LoggingLevel.ERROR, LoggingLevel.INFO, LoggingLevel.DEBUG.)
 * @param {string} message The message to log to the console.
 */
function logMessage(loggingLevel, message) {
    if (loggingLevel.numericLevel < minimumLoggingLevel.numericLevel)
        return

    var currentDate = new Date()
    print("[" + currentDate.getHours() + ":" + currentDate.getMinutes() + ":" + currentDate.getSeconds() + "] [ISawedThisPlayerInHalf/PlayerOffsetterCore/" +
        loggingLevel.name + "]: " + message)
}

/**
 * Logs that a transform was successful, showing the transformed function and the host class's name.
 *
 * @param {string} functionName The name of the function that was transformed.
 * @param {string} fullClassName The full name of the class that the function resides in.
 */
function logTransformSuccess(functionName, fullClassName) {
    logMessage(LoggingLevel.DEBUG, "Successfully transformed " + functionName + " in " + fullClassName)
}

/**
 * Logs that a transform was not successful, showing the function that was being transformed, the host class's name, and what went wrong.
 *
 * @param {string} functionName The name of the function that was transformed.
 * @param {string} fullClassName The full name of the class that the function resides in.
 */
function logTransformError(functionName, fullClassName, errorMessage) {
    logMessage(LoggingLevel.ERROR, "An error occurred while transforming " + functionName + " in " + fullClassName + ":\n    " + errorMessage)
}



function initializeCoreMod() {
    return {
        /**
         * Adds an offset to the raycast that finds what block an entity is looking at.
         */
        "Entity": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.entity.Entity"
            },

            "transformer": function(classNode) {
                var pick = findObfuscatedMethodWithSignature(classNode, "pick", "func_213324_a", "(DFZ)Lnet/minecraft/util/math/RayTraceResult;")

                if (pick !== null) {
                    try {
                        var oldInstructions = pick.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            if (checkObfuscatedMethodInsn(oldInstructions.get(i), Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEyePosition", "func_174824_e",
                                    "(F)Lnet/minecraft/util/math/Vec3d;")) {
                                var offsetRaycastInstructions = new InsnList()

                                offsetRaycastInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0)) // this Lnet/minecraft/entity/Entity;
                                offsetRaycastInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "offsetVector",
                                    "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))

                                // ...
                                // INVOKEVIRTUAL net/minecraft/entity/Entity.getEyePosition (F)Lnet/minecraft/util/math/Vec3d;
                                oldInstructions.insert(oldInstructions.get(i), offsetRaycastInstructions)
                                // ...

                                success = true
                                logTransformSuccess("function pick", "net.minecraft.entity.Entity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function pick", "net.minecraft.entity.Entity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function pick", "net.minecraft.entity.Entity", exception.message)
                    }

                } else
                    logTransformError("function pick", "net.minecraft.entity.Entity", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Alters knockback to account for offsets.
         */
        "LivingEntity": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.entity.LivingEntity"
            },

            "transformer": function(classNode) {
                var attackEntityFrom = findObfuscatedMethodWithSignature(classNode, "attackEntityFrom", "func_70097_a",
                    "(Lnet/minecraft/util/DamageSource;F)Z")

                if (attackEntityFrom !== null) {
                    try {
                        var oldInstructions = attackEntityFrom.instructions
                        success = false

                        for (var i = 0; i <= oldInstructions.size() - 6; i++) {
                            if (checkVarInsn(oldInstructions.get(i), Opcodes.ALOAD, 0) && checkVarInsn(oldInstructions.get(i+1), Opcodes.ALOAD, 7)
                                    && checkVarInsn(oldInstructions.get(i+3), Opcodes.DLOAD, 8) && checkVarInsn(oldInstructions.get(i+4), Opcodes.DLOAD, 10)
                                    && checkObfuscatedMethodInsn(oldInstructions.get(i+5), Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/LivingEntity", "knockBack", "func_70653_a", "(Lnet/minecraft/entity/Entity;FDD)V")) {
                                var offsetKnockback = new InsnList()


                                offsetKnockback.add(new VarInsnNode(Opcodes.ALOAD, 7)) // entity1 Lnet/minecraft/entity/Entity;
                                offsetKnockback.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "getOffsets",
                                    "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))

                                offsetKnockback.add(new InsnNode(Opcodes.DUP))
                                offsetKnockback.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorX",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetKnockback.add(new VarInsnNode(Opcodes.DLOAD, 8)) // d1 D
                                offsetKnockback.add(new InsnNode(Opcodes.DADD))
                                offsetKnockback.add(new VarInsnNode(Opcodes.DSTORE, 8)) // d1 D

                                offsetKnockback.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorZ",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetKnockback.add(new VarInsnNode(Opcodes.DLOAD, 10)) // d0 D
                                offsetKnockback.add(new InsnNode(Opcodes.DADD))
                                offsetKnockback.add(new VarInsnNode(Opcodes.DSTORE, 10)) // d0 D


                                // ...
                                oldInstructions.insertBefore(oldInstructions.get(i), offsetKnockback)
                                // ALOAD 0
                                // ALOAD 7
                                // ?LDC 0.4?
                                // DLOAD 8
                                // DLOAD 10
                                // INVOKEVIRTUAL net/minecraft/entity/LivingEntity.knockBack (Lnet/minecraft/entity/Entity;FDD)V
                                // ...

                                success = true
                                logTransformSuccess("function attackEntityFrom", "net.minecraft.entity.LivingEntity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function attackEntityFrom", "net.minecraft.entity.LivingEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function attackEntityFrom", "net.minecraft.entity.LivingEntity", exception.message)
                    }

                } else
                    logTransformError("function attackEntityFrom", "net.minecraft.entity.LivingEntity", "Unable to function to transform")

                return classNode
            }
        },

        /**
         * Modifies the home position and the position they are pulled to for leashed animals to account for offsets.
         * Modifies the distance calculations for when to change the movement type and to break the leash.
         */
        "CreatureEntity": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.entity.CreatureEntity"
            },

            "transformer": function(classNode) {
                var updateLeashedState = findObfuscatedMethodWithSignature(classNode, "updateLeashedState", "func_110159_bB", "()V")

                if (updateLeashedState !== null) {
                    var oldInstructions = updateLeashedState.instructions

                    // Modifies home position set for leashes.
                    try {
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            if (checkMethodInsn(oldInstructions.get(i), Opcodes.INVOKESPECIAL, "net/minecraft/util/math/BlockPos", "<init>", "(Lnet/minecraft/entity/Entity;)V")) {
                                var offsetHomePosition = new InsnList()

                                offsetHomePosition.add(new VarInsnNode(Opcodes.ALOAD, 1)) // entity Lnet/minecraft/entity/Entity;
                                offsetHomePosition.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "offsetBlockPosition",
                                    "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/BlockPos;",
                                    false
                                ))

                                // ...
                                // INVOKESPECIAL net/minecraft/util/math/BlockPos.<init> (Lnet/minecraft/entity/Entity;)V
                                oldInstructions.insert(oldInstructions.get(i), offsetHomePosition)
                                // ...

                                success = true
                                logTransformSuccess("first area of function updateLeashedState", "net.minecraft.entity.CreatureEntity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("first area of function updateLeashedState", "net.minecraft.entity.CreatureEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("first area of function updateLeashedState", "net.minecraft.entity.CreatureEntity", exception.message)
                    }

                    // Modifies distance calculation for leashes.
                    try {
                        var success = false

                        for (var i = 0; i <= oldInstructions.size() - 4; i++) {
                            var firstInstruction = oldInstructions.get(i)
                            var lastInstruction = oldInstructions.get(i+3)

                            if (checkVarInsn(firstInstruction, Opcodes.ALOAD, 0) && checkVarInsn(oldInstructions.get(i+1), Opcodes.ALOAD, 1)
                                    && checkObfuscatedMethodInsn(oldInstructions.get(i+2), Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getDistance", "func_70032_d", "(Lnet/minecraft/entity/Entity;)F")
                                    && checkVarInsn(lastInstruction, Opcodes.FSTORE, 2)) {
                                var redoGetDistance = new InsnList()
                                var skipOriginal = new LabelNode()

                                redoGetDistance.add(skipOriginal)
                                redoGetDistance.add(new VarInsnNode(Opcodes.ALOAD, 0)) // this Lnet/minecraft/entity/CreatureEntity;
                                redoGetDistance.add(new VarInsnNode(Opcodes.ALOAD, 1)) // entity Lnet/minecraft/entity/Entity;
                                redoGetDistance.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "modifiedGetDistance",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;)F",
                                    false
                                ))
                                redoGetDistance.add(new VarInsnNode(Opcodes.FSTORE, 2)) // f F

                                // ...
                                oldInstructions.insertBefore(firstInstruction, new JumpInsnNode(Opcodes.GOTO, skipOriginal))
                                // ALOAD 0
                                // ALOAD 1
                                // INVOKEVIRTUAL net/minecraft/entity/CreatureEntity.getDistance (Lnet/minecraft/entity/Entity;)F
                                // FSTORE 2
                                oldInstructions.insert(lastInstruction, redoGetDistance)
                                // ...

                                success = true
                                logTransformSuccess("second area of function updateLeashedState", "net.minecraft.entity.CreatureEntity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("second area of function updateLeashedState", "net.minecraft.entity.CreatureEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("second area of function updateLeashedState", "net.minecraft.entity.CreatureEntity", exception.message)
                    }

                    // Modifies pull position for leashes.
                    try {
                        var success = false

                        for (var i = 0; i <= oldInstructions.size() - 6; i++) {
                            if (checkObfuscatedMethodInsn(oldInstructions.get(i), Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getPosZ", "func_226281_cx_", "()D")
                                    && checkInsn(oldInstructions.get(i+1), Opcodes.DSUB) && checkVarInsn(oldInstructions.get(i+2), Opcodes.FLOAD, 2) && checkInsn(oldInstructions.get(i+3), Opcodes.F2D)
                                    && checkInsn(oldInstructions.get(i+4), Opcodes.DDIV) && checkVarInsn(oldInstructions.get(i+5), Opcodes.DSTORE, 7)) {
                                var offsetSetMotion = new InsnList()


                                offsetSetMotion.add(new VarInsnNode(Opcodes.ALOAD, 1)) // entity Lnet/minecraft/entity/Entity;
                                offsetSetMotion.add(new VarInsnNode(Opcodes.FLOAD, 2)) // f F
                                offsetSetMotion.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getOffsetsInverselyScaled",
                                    "(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))

                                offsetSetMotion.add(new InsnNode(Opcodes.DUP))
                                offsetSetMotion.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorX",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetSetMotion.add(new VarInsnNode(Opcodes.DLOAD, 3)) // d0 D
                                offsetSetMotion.add(new InsnNode(Opcodes.DADD))
                                offsetSetMotion.add(new VarInsnNode(Opcodes.DSTORE, 3)) // d0 D

                                offsetSetMotion.add(new InsnNode(Opcodes.DUP))
                                offsetSetMotion.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorY",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetSetMotion.add(new VarInsnNode(Opcodes.DLOAD, 5)) // d1 D
                                offsetSetMotion.add(new InsnNode(Opcodes.DADD))
                                offsetSetMotion.add(new VarInsnNode(Opcodes.DSTORE, 5)) // d1 D

                                offsetSetMotion.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorZ",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetSetMotion.add(new VarInsnNode(Opcodes.DLOAD, 7)) // d2 D
                                offsetSetMotion.add(new InsnNode(Opcodes.DADD))
                                offsetSetMotion.add(new VarInsnNode(Opcodes.DSTORE, 7)) // d2 D


                                // ...
                                // INVOKEVIRTUAL net/minecraft/entity/CreatureEntity.getPosZ ()D
                                // DSUB
                                // FLOAD 2
                                // F2D
                                // DDIV
                                // DSTORE 7
                                oldInstructions.insert(oldInstructions.get(i+5), offsetSetMotion)
                                // ...

                                success = true
                                logTransformSuccess("third area of function updateLeashedState", "net.minecraft.entity.CreatureEntity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("third area of function updateLeashedState", "net.minecraft.entity.CreatureEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("third area of function updateLeashedState", "net.minecraft.entity.CreatureEntity", exception.message)
                    }

                    // Modifies AI move behavior when leashed.
                    try {
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            if (checkMethodInsn(oldInstructions.get(i), Opcodes.INVOKESPECIAL, "net/minecraft/util/math/Vec3d", "<init>", "(DDD)V")) {
                                var offsetTryMoveTo = new InsnList()

                                offsetTryMoveTo.add(new VarInsnNode(Opcodes.ALOAD, 1)) // entity Lnet/minecraft/entity/Entity;
                                offsetTryMoveTo.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "offsetVector",
                                    "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))

                                // ...
                                // INVOKESPECIAL net/minecraft/util/math/Vec3d.<init> (DDD)V
                                oldInstructions.insert(oldInstructions.get(i), offsetTryMoveTo)
                                // ...

                                success = true
                                logTransformSuccess("fourth area of function updateLeashedState", "net.minecraft.entity.CreatureEntity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("fourth area of  function updateLeashedState", "net.minecraft.entity.CreatureEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("fourth area of function updateLeashedState", "net.minecraft.entity.CreatureEntity", exception.message)
                    }

                } else
                    logTransformError("function updateLeashedState", "net.minecraft.entity.CreatureEntity", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Offsets arrows shot by players.
         */
        "AbstractArrowEntity": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.entity.projectile.AbstractArrowEntity"
            },

            "transformer": function(classNode) {
                var init = findMethodWithSignature(classNode, "<init>", "(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;)V")

                if (init !== null) {
                    try {
                        var oldInstructions = init.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            if (checkMethodInsn(oldInstructions.get(i), Opcodes.INVOKESPECIAL, "net/minecraft/entity/projectile/AbstractArrowEntity", "<init>",
                                    "(Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V")) {
                                var offsetProjectileInstructions = new InsnList()

                                offsetProjectileInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0)) // this Lnet/minecraft/entity/projectile/AbstractArrowEntity;
                                offsetProjectileInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2)) // shooter Lnet/minecraft/entity/LivingEntity;
                                offsetProjectileInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "offsetProjectile",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;)V",
                                    false
                                ))

                                // ...
                                // INVOKESPECIAL net/minecraft/entity/projectile/AbstractArrowEntity.<init> (Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V
                                oldInstructions.insert(oldInstructions.get(i), offsetProjectileInstructions)
                                // ...

                                success = true
                                logTransformSuccess("constructor", "net.minecraft.entity.projectile.AbstractArrowEntity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("constructor", "net.minecraft.entity.projectile.AbstractArrowEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("constructor", "net.minecraft.entity.projectile.AbstractArrowEntity", exception.message)
                    }

                } else
                    logTransformError("constructor", "net.minecraft.entity.projectile.AbstractArrowEntity", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Offsets throwables thrown by players.
         */
        "ThrowableEntity": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.entity.projectile.ThrowableEntity"
            },

            "transformer": function(classNode) {
                var init = findMethodWithSignature(classNode, "<init>", "(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;)V")

                if (init !== null) {
                    try {
                        var oldInstructions = init.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            if (checkMethodInsn(oldInstructions.get(i), Opcodes.INVOKESPECIAL, "net/minecraft/entity/projectile/ThrowableEntity", "<init>",
                                    "(Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V")) {
                                var offsetProjectileInstructions = new InsnList()

                                offsetProjectileInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0)) // this Lnet/minecraft/entity/projectile/ThrowableEntity;
                                offsetProjectileInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2)) // livingEntityIn Lnet/minecraft/entity/LivingEntity;
                                offsetProjectileInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "offsetProjectile",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;)V",
                                    false
                                ))

                                // ...
                                // INVOKESPECIAL net/minecraft/entity/projectile/ThrowableEntity.<init> (Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V
                                oldInstructions.insert(oldInstructions.get(i), offsetProjectileInstructions)
                                // ...

                                success = true
                                logTransformSuccess("constructor", "net.minecraft.entity.projectile.ThrowableEntity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("constructor", "net.minecraft.entity.projectile.ThrowableEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("constructor", "net.minecraft.entity.projectile.ThrowableEntity", exception.message)
                    }

                } else
                    logTransformError("constructor", "net.minecraft.entity.projectile.ThrowableEntity", "Unable to find function to transform")

                return classNode
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
                var shouldStopFishing = findObfuscatedMethodWithSignature(classNode, "shouldStopFishing", "func_190625_o", "()Z")

                if (shouldStopFishing !== null) {
                    try {
                        var oldInstructions = shouldStopFishing.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getDistanceSq",
                                    "func_70068_e", "(Lnet/minecraft/entity/Entity;)D")) {
                                var redoGetDistanceSq = new InsnList()
                                var skipOriginal = new LabelNode()

                                redoGetDistanceSq.add(skipOriginal)
                                redoGetDistanceSq.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;)D",
                                    false
                                ))

                                // ...
                                oldInstructions.insertBefore(instruction, new JumpInsnNode(Opcodes.GOTO, skipOriginal))
                                // INVOKEVIRTUAL net/minecraft/entity/projectile/FishingBobberEntity.getDistanceSq (Lnet/minecraft/entity/Entity;)D
                                oldInstructions.insert(instruction, redoGetDistanceSq)
                                // ...

                                success = true
                                logTransformSuccess("function shouldStopFishing", "net.minecraft.entity.projectile.FishingBobberEntity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function shouldStopFishing", "net.minecraft.entity.projectile.FishingBobberEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function shouldStopFishing", "net.minecraft.entity.projectile.FishingBobberEntity", exception.message)
                    }

                } else
                    logTransformError("function shouldStopFishing", "net.minecraft.entity.projectile.FishingBobberEntity", "Unable to find function to transform")


                // Offsets the destination of reeled-in items.
                var handleHookRetraction = findObfuscatedMethodWithSignature(classNode, "handleHookRetraction", "func_146034_e",
                    "(Lnet/minecraft/item/ItemStack;)I")

                if (handleHookRetraction !== null) {
                    try {
                        var oldInstructions = handleHookRetraction.instructions
                        var success = false

                        for (var i = 0; i <= oldInstructions.size() - 3; i++) {
                            if (checkObfuscatedMethodInsn(oldInstructions.get(i), Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getPosZ", "func_226281_cx_", "()D")
                                    && checkInsn(oldInstructions.get(i+1), Opcodes.DSUB) && checkVarInsn(oldInstructions.get(i+2), Opcodes.DSTORE, 14)) {
                                var offsetAnglerPosition = new InsnList()

                                offsetAnglerPosition.add(new VarInsnNode(Opcodes.ALOAD, 0)) // this Lnet/minecraft/entity/projectile/FishingBobberEntity;
                                offsetAnglerPosition.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getAnglerOffsets",
                                    "(Lnet/minecraft/entity/projectile/FishingBobberEntity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))

                                offsetAnglerPosition.add(new InsnNode(Opcodes.DUP))
                                offsetAnglerPosition.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorX",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetAnglerPosition.add(new VarInsnNode(Opcodes.DLOAD, 10)) // d0 D
                                offsetAnglerPosition.add(new InsnNode(Opcodes.DADD))
                                offsetAnglerPosition.add(new VarInsnNode(Opcodes.DSTORE, 10)) // d0 D

                                offsetAnglerPosition.add(new InsnNode(Opcodes.DUP))
                                offsetAnglerPosition.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorY",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetAnglerPosition.add(new VarInsnNode(Opcodes.DLOAD, 12)) // d1 D
                                offsetAnglerPosition.add(new InsnNode(Opcodes.DADD))
                                offsetAnglerPosition.add(new VarInsnNode(Opcodes.DSTORE, 12)) // d1 D

                                offsetAnglerPosition.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorZ",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetAnglerPosition.add(new VarInsnNode(Opcodes.DLOAD, 14)) // d2 D
                                offsetAnglerPosition.add(new InsnNode(Opcodes.DADD))
                                offsetAnglerPosition.add(new VarInsnNode(Opcodes.DSTORE, 14)) // d2 D


                                // ...
                                // INVOKEVIRTUAL net/minecraft/entity/projectile/FishingBobberEntity.getPosZ ()D
                                // DSUB
                                // DSTORE 14
                                oldInstructions.insert(oldInstructions.get(i+2), offsetAnglerPosition)
                                // ...

                                logTransformSuccess("function handleHookRetraction", "net.minecraft.entity.projectile.FishingBobberEntity")
                                success = true

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function handleHookRetraction", "net.minecraft.entity.projectile.FishingBobberEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function handleHookRetraction", "net.minecraft.entity.projectile.FishingBobberEntity", exception.message)
                    }

                } else
                    logTransformError("function handleHookRetraction", "net.minecraft.entity.projectile.FishingBobberEntity", "Unable to find function to transform")


                // Offsets the destination of reeled-in entities and Entities.
                var bringInHookedEntity = findObfuscatedMethodWithSignature(classNode, "bringInHookedEntity", "func_184527_k", "()V")

                if (bringInHookedEntity !== null) {
                    try {
                        var oldInstructions = bringInHookedEntity.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            if (checkMethodInsn(oldInstructions.get(i), Opcodes.INVOKESPECIAL, "net/minecraft/util/math/Vec3d", "<init>", "(DDD)V")) {
                                var offsetBringInPosition = new InsnList()

                                offsetBringInPosition.add(new VarInsnNode(Opcodes.ALOAD, 0)) // this Lnet/minecraft/entity/projectile/FishingBobberEntity;
                                offsetBringInPosition.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "offsetVectorWithAngler",
                                    "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/projectile/FishingBobberEntity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))

                                // ...
                                // INVOKESPECIAL net/minecraft/util/math/Vec3d.<init> (DDD)V
                                oldInstructions.insert(oldInstructions.get(i), offsetBringInPosition)
                                // ...

                                logTransformSuccess("function bringInHookedEntity", "net.minecraft.entity.projectile.FishingBobberEntity")
                                success = true

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function bringInHookedEntity", "net.minecraft.entity.projectile.FishingBobberEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function bringInHookedEntity", "net.minecraft.entity.projectile.FishingBobberEntity", exception.message)
                    }

                } else
                    logTransformError("function bringInHookedEntity", "net.minecraft.entity.projectile.FishingBobberEntity", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Allows players to keep using container minecarts relative to their offsets.
         */
        "ContainerMinecartEntity": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.entity.item.minecart.ContainerMinecartEntity"
            },

            "transformer": function(classNode) {
                var isUsableByPlayer = findObfuscatedMethodWithSignature(classNode, "isUsableByPlayer", "func_70300_a",
                    "(Lnet/minecraft/entity/player/PlayerEntity;)Z")

                if (isUsableByPlayer !== null) {
                    try {
                        var oldInstructions = isUsableByPlayer.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getDistanceSq",
                                    "func_70068_e", "(Lnet/minecraft/entity/Entity;)D")) {
                                var redoGetDistanceSq = new InsnList()
                                var skipOriginal = new LabelNode()

                                redoGetDistanceSq.add(skipOriginal)
                                redoGetDistanceSq.add(new InsnNode(Opcodes.SWAP))
                                redoGetDistanceSq.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;)D",
                                    false
                                ))

                                // ...
                                oldInstructions.insertBefore(instruction, new JumpInsnNode(Opcodes.GOTO, skipOriginal))
                                // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getDistanceSq (Lnet/minecraft/entity/Entity;)D
                                oldInstructions.insert(instruction, redoGetDistanceSq)
                                // ...

                                success = true
                                logTransformSuccess("function isUsableByPlayer", "net.minecraft.entity.item.minecart.ContainerMinecartEntity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function isUsableByPlayer", "net.minecraft.entity.item.minecart.ContainerMinecartEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function isUsableByPlayer", "net.minecraft.entity.item.minecart.ContainerMinecartEntity", exception.message)
                    }

                } else
                    logTransformError("function isUsableByPlayer", "net.minecraft.entity.item.minecart.ContainerMinecartEntity", "Unable to find function to transform")

                return classNode
            }
        },


        /**
         * Makes entities look at the offset position of players randomly, resulting in them shaking their heads 'out of confusion.'
         */
        "LookAtGoal": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.entity.ai.goal.LookAtGoal"
            },

            "transformer": function(classNode) {
                var tick = findObfuscatedMethodWithSignature(classNode, "tick", "func_75246_d", "()V")

                if (tick !== null) {
                    try {
                        var oldInstructions = tick.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            if (checkObfuscatedMethodInsn(oldInstructions.get(i), Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/ai/controller/LookController", "setLookPosition", "func_220679_a", "(DDD)V")) {
                                var offsetLookAtPosition = new InsnList()

                                // 50% chance to set the position to be looked at to the player's offset one.
                                offsetLookAtPosition.add(new VarInsnNode(Opcodes.ALOAD, 0))
                                offsetLookAtPosition.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "applyLookAtOffsetsRandomly",
                                    "(DDDLnet/minecraft/entity/ai/goal/LookAtGoal;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))
                                // Unpacks Vec3d.
                                offsetLookAtPosition.add(new InsnNode(Opcodes.DUP))
                                offsetLookAtPosition.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorX",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetLookAtPosition.add(new InsnNode(Opcodes.DUP2_X1))
                                offsetLookAtPosition.add(new InsnNode(Opcodes.POP2))
                                offsetLookAtPosition.add(new InsnNode(Opcodes.DUP))
                                offsetLookAtPosition.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorY",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetLookAtPosition.add(new InsnNode(Opcodes.DUP2_X1))
                                offsetLookAtPosition.add(new InsnNode(Opcodes.POP2))
                                offsetLookAtPosition.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorZ",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))

                                // ...
                                oldInstructions.insertBefore(oldInstructions.get(i), offsetLookAtPosition)
                                // INVOKEVIRTUAL net/minecraft/entity/ai/controller/LookController.setLookPosition (DDD)V
                                // ...

                                success = true
                                logTransformSuccess("function tick", "net.minecraft.entity.ai.goal.LookAtGoal")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function tick", "net.minecraft.entity.ai.goal.LookAtGoal", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function tick", "net.minecraft.entity.ai.goal.LookAtGoal", exception.message)
                    }

                } else
                    logTransformError("function tick", "net.minecraft.entity.ai.goal.LookAtGoal", "Unable to find function to transform")

                return classNode
            }
        },


        /**
         * Allows players to keep using containers relative to their offsets.
         */
        "Container": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.inventory.container.Container"
            },

            "transformer": function(classNode) {
                var lambda$isWithinUsableDistance$0 = findObfuscatedMethodWithSignature(classNode, "lambda$isWithinUsableDistance$0",
                    "lambda$func_216963_a$0", "(Lnet/minecraft/block/Block;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Boolean;")

                if (lambda$isWithinUsableDistance$0 !== null) {
                    try {
                        var oldInstructions = lambda$isWithinUsableDistance$0.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getDistanceSq",
                                    "func_70092_e", "(DDD)D")) {
                                var redoGetDistanceSq = new InsnList()
                                var skipOriginal = new LabelNode()

                                redoGetDistanceSq.add(skipOriginal)
                                redoGetDistanceSq.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;DDD)D",
                                    false
                                ))

                                // ...
                                oldInstructions.insertBefore(instruction, new JumpInsnNode(Opcodes.GOTO, skipOriginal))
                                // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getDistanceSq (DDD)D
                                oldInstructions.insert(instruction, redoGetDistanceSq)
                                // ...

                                success = true
                                logTransformSuccess("function lambda$isWithinUsableDistance$0", "net.minecraft.inventory.container.Container")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function lambda$isWithinUsableDistance$0", "net.minecraft.inventory.container.Container", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function lambda$isWithinUsableDistance$0", "net.minecraft.inventory.container.Container", exception.message)
                    }

                } else
                    logTransformError("function lambda$isWithinUsableDistance$0", "net.minecraft.inventory.container.Container", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Allows players to keep using storage inventories relative to their offsets.
         */
        "LockableLootTileEntity": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.tileentity.LockableLootTileEntity"
            },

            "transformer": function(classNode) {
                var isUsableByPlayer = findObfuscatedMethodWithSignature(classNode, "isUsableByPlayer", "func_70300_a",
                    "(Lnet/minecraft/entity/player/PlayerEntity;)Z")

                if (isUsableByPlayer !== null) {
                    try {
                        var oldInstructions = isUsableByPlayer.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getDistanceSq",
                                    "func_70092_e", "(DDD)D")) {
                                var redoGetDistanceSq = new InsnList()
                                var skipOriginal = new LabelNode()

                                redoGetDistanceSq.add(skipOriginal)
                                redoGetDistanceSq.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;DDD)D",
                                    false
                                ))

                                // ...
                                oldInstructions.insertBefore(instruction, new JumpInsnNode(Opcodes.GOTO, skipOriginal))
                                // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getDistanceSq (DDD)D
                                oldInstructions.insert(instruction, redoGetDistanceSq)
                                // ...

                                success = true
                                logTransformSuccess("function isUsableByPlayer", "net.minecraft.tileentity.LockableLootTileEntity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function isUsableByPlayer", "net.minecraft.tileentity.LockableLootTileEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function isUsableByPlayer", "net.minecraft.tileentity.LockableLootTileEntity", exception.message)
                    }

                } else
                    logTransformError("function isUsableByPlayer", "net.minecraft.tileentity.LockableLootTileEntity", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Allows players to keep using ender chests relative to their offsets.
         */
        "EnderChestTileEntity": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.tileentity.EnderChestTileEntity"
            },

            "transformer": function(classNode) {
                var canBeUsed = findObfuscatedMethodWithSignature(classNode, "canBeUsed", "func_145971_a", "(Lnet/minecraft/entity/player/PlayerEntity;)Z")

                if (canBeUsed !== null) {
                    try {
                        var oldInstructions = canBeUsed.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getDistanceSq",
                                    "func_70092_e", "(DDD)D")) {
                                var redoGetDistanceSq = new InsnList()
                                var skipOriginal = new LabelNode()

                                redoGetDistanceSq.add(skipOriginal)
                                redoGetDistanceSq.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;DDD)D",
                                    false
                                ))

                                // ...
                                oldInstructions.insertBefore(instruction, new JumpInsnNode(Opcodes.GOTO, skipOriginal))
                                // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getDistanceSq (DDD)D
                                oldInstructions.insert(instruction, redoGetDistanceSq)
                                // ...

                                success = true
                                logTransformSuccess("function canBeUsed", "net.minecraft.tileentity.EnderChestTileEntity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function canBeUsed", "net.minecraft.tileentity.EnderChestTileEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function canBeUsed", "net.minecraft.tileentity.EnderChestTileEntity", exception.message)
                    }

                } else
                    logTransformError("function canBeUsed", "net.minecraft.tileentity.EnderChestTileEntity", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Allows players to keep using lecterns relative to their offsets.
         */
        "LecternTileEntity": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.tileentity.LecternTileEntity$1"
            },

            "transformer": function(classNode) {
                var isUsableByPlayer = findObfuscatedMethodWithSignature(classNode, "isUsableByPlayer", "func_70300_a",
                    "(Lnet/minecraft/entity/player/PlayerEntity;)Z")

                if (isUsableByPlayer !== null) {
                    try {
                        var oldInstructions = isUsableByPlayer.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getDistanceSq",
                                    "func_70092_e", "(DDD)D")) {
                                var redoGetDistanceSq = new InsnList()
                                var skipOriginal = new LabelNode()

                                redoGetDistanceSq.add(skipOriginal)
                                redoGetDistanceSq.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;DDD)D",
                                    false
                                ))

                                // ...
                                oldInstructions.insertBefore(instruction, new JumpInsnNode(Opcodes.GOTO, skipOriginal))
                                // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getDistanceSq (DDD)D
                                oldInstructions.insert(instruction, redoGetDistanceSq)
                                // ...

                                success = true
                                logTransformSuccess("function isUsableByPlayer", "net.minecraft.tileentity.LecternTileEntity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function isUsableByPlayer", "net.minecraft.tileentity.LecternTileEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function isUsableByPlayer", "net.minecraft.tileentity.LecternTileEntity", exception.message)
                    }

                } else
                    logTransformError("function isUsableByPlayer", "net.minecraft.tileentity.LecternTileEntity", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Allows players to keep using furnace inventories relative to their offsets.
         */
        "AbstractFurnaceTileEntity": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.tileentity.AbstractFurnaceTileEntity"
            },

            "transformer": function(classNode) {
                var isUsableByPlayer = findObfuscatedMethodWithSignature(classNode, "isUsableByPlayer", "func_70300_a",
                    "(Lnet/minecraft/entity/player/PlayerEntity;)Z")

                if (isUsableByPlayer !== null) {
                    try {
                        var oldInstructions = isUsableByPlayer.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getDistanceSq",
                                    "func_70092_e", "(DDD)D")) {
                                var redoGetDistanceSq = new InsnList()
                                var skipOriginal = new LabelNode()

                                redoGetDistanceSq.add(skipOriginal)
                                redoGetDistanceSq.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;DDD)D",
                                    false
                                ))

                                // ...
                                oldInstructions.insertBefore(instruction, new JumpInsnNode(Opcodes.GOTO, skipOriginal))
                                // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getDistanceSq (DDD)D
                                oldInstructions.insert(instruction, redoGetDistanceSq)
                                // ...

                                success = true
                                logTransformSuccess("function isUsableByPlayer", "net.minecraft.tileentity.AbstractFurnaceTileEntity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function isUsableByPlayer", "net.minecraft.tileentity.AbstractFurnaceTileEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function isUsableByPlayer", "net.minecraft.tileentity.AbstractFurnaceTileEntity", exception.message)
                    }

                } else
                    logTransformError("function isUsableByPlayer", "net.minecraft.tileentity.AbstractFurnaceTileEntity", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Allows players to keep using brewing stand inventories relative to their offsets.
         */
        "BrewingStandTileEntity": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.tileentity.BrewingStandTileEntity"
            },

            "transformer": function(classNode) {
                var isUsableByPlayer = findObfuscatedMethodWithSignature(classNode, "isUsableByPlayer", "func_70300_a",
                    "(Lnet/minecraft/entity/player/PlayerEntity;)Z")

                if (isUsableByPlayer !== null) {
                    try {
                        var oldInstructions = isUsableByPlayer.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getDistanceSq",
                                    "func_70092_e", "(DDD)D")) {
                                var redoGetDistanceSq = new InsnList()
                                var skipOriginal = new LabelNode()

                                redoGetDistanceSq.add(skipOriginal)
                                redoGetDistanceSq.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;DDD)D",
                                    false
                                ))

                                // ...
                                oldInstructions.insertBefore(instruction, new JumpInsnNode(Opcodes.GOTO, skipOriginal))
                                // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getDistanceSq (DDD)D
                                oldInstructions.insert(instruction, redoGetDistanceSq)
                                // ...

                                success = true
                                logTransformSuccess("function isUsableByPlayer", "net.minecraft.tileentity.BrewingStandTileEntity")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function isUsableByPlayer", "net.minecraft.tileentity.BrewingStandTileEntity", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function isUsableByPlayer", "net.minecraft.tileentity.BrewingStandTileEntity", exception.message)
                    }

                } else
                    logTransformError("function isUsableByPlayer", "net.minecraft.tileentity.BrewingStandTileEntity", "Unable to find function to transform")

                return classNode
            }
        },


        /**
         * Offsets cast fishing bobbers.
         */
        "FishingRodItem": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.item.FishingRodItem"
            },

            "transformer": function(classNode) {
                var onItemRightClick = findObfuscatedMethodWithSignature(classNode, "onItemRightClick", "func_77659_a",
                    "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;")

                if (onItemRightClick !== null) {
                    try {
                        var oldInstructions = onItemRightClick.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            if (checkMethodInsn(oldInstructions.get(i), Opcodes.INVOKESPECIAL, "net/minecraft/entity/projectile/FishingBobberEntity", "<init>",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;II)V")) {
                                var offsetBobberInstructions = new InsnList()

                                offsetBobberInstructions.add(new InsnNode(Opcodes.DUP))
                                offsetBobberInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2)) // playerIn Lnet/minecraft/entity/player/PlayerEntity;
                                offsetBobberInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "offsetProjectile",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;)V",
                                    false
                                ))

                                // ...
                                // INVOKESPECIAL net/minecraft/entity/projectile/FishingBobberEntity.<init> (Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;II)V
                                oldInstructions.insert(oldInstructions.get(i), offsetBobberInstructions)
                                // ...

                                success = true
                                logTransformSuccess("function onItemRightClick", "net.minecraft.item.FishingRodItem")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function onItemRightClick", "net.minecraft.item.FishingRodItem", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function onItemRightClick", "net.minecraft.item.FishingRodItem", exception.message)
                    }

                } else
                    logTransformError("function onItemRightClick", "net.minecraft.item.FishingRodItem", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Offsets eyes of ender launched by players.
         */
        "EnderEyeItem": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.item.EnderEyeItem"
            },

            "transformer": function(classNode) {
                var onItemRightClick = findObfuscatedMethodWithSignature(classNode, "onItemRightClick", "func_77659_a",
                    "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;")

                if (onItemRightClick !== null) {
                    try {
                        var oldInstructions = onItemRightClick.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            if (checkMethodInsn(oldInstructions.get(i), Opcodes.INVOKESPECIAL, "net/minecraft/entity/item/EyeOfEnderEntity", "<init>",
                                    "(Lnet/minecraft/world/World;DDD)V")) {
                                var offsetProjectileInstructions = new InsnList()

                                offsetProjectileInstructions.add(new InsnNode(Opcodes.DUP))
                                offsetProjectileInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2)) // playerIn Lnet/minecraft/entity/player/PlayerEntity;
                                offsetProjectileInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "offsetProjectile",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;)V",
                                    false
                                ))

                                // ...
                                // INVOKESPECIAL net/minecraft/entity/item/EyeOfEnderEntity.<init> (Lnet/minecraft/world/World;DDD)V
                                oldInstructions.insert(oldInstructions.get(i), offsetProjectileInstructions)
                                // ...

                                success = true
                                logTransformSuccess("function onItemRightClick", "net.minecraft.item.EnderEyeItem")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function onItemRightClick", "net.minecraft.item.EnderEyeItem", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function onItemRightClick", "net.minecraft.item.EnderEyeItem", exception.message)
                    }

                } else
                    logTransformError("function onItemRightClick", "net.minecraft.item.EnderEyeItem", "Unable to find function to transform")

                return classNode
            }
        },


        /**
         * Allows players to break far away blocks.
         */
        "PlayerInteractionManager": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.server.management.PlayerInteractionManager"
            },

            "transformer": function(classNode) {
                var func_225416_a = findMethodWithSignature(classNode, "func_225416_a",
                    "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/network/play/client/CPlayerDiggingPacket$Action;Lnet/minecraft/util/Direction;I)V")

                if (func_225416_a !== null) {
                    try {
                        var oldInstructions = func_225416_a.instructions
                        var success = false

                        for (var i = 0; i <= oldInstructions.size() - 7; i++) {
                            if (checkVarInsn(oldInstructions.get(i), Opcodes.ALOAD, 1) && checkObfuscatedMethodInsn(oldInstructions.get(i+1), Opcodes.INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", "getZ", "func_177952_p", "()I")
                                    && checkInsn(oldInstructions.get(i+2), Opcodes.I2D) && checkLdcInsn(oldInstructions.get(i+3), 0.5) && checkInsn(oldInstructions.get(i+4), Opcodes.DADD)
                                    && checkInsn(oldInstructions.get(i+5), Opcodes.DSUB) && checkVarInsn(oldInstructions.get(i+6), Opcodes.DSTORE, 9)) {
                                var offsetPlayerPosition = new InsnList()


                                offsetPlayerPosition.add(new VarInsnNode(Opcodes.ALOAD, 0)) // this Lnet/minecraft/server/management/PlayerInteractionManager;
                                offsetPlayerPosition.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getOffsetsFromManager",
                                    "(Lnet/minecraft/server/management/PlayerInteractionManager;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))

                                offsetPlayerPosition.add(new InsnNode(Opcodes.DUP))
                                offsetPlayerPosition.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorX",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetPlayerPosition.add(new VarInsnNode(Opcodes.DLOAD, 5)) // d0 D
                                offsetPlayerPosition.add(new InsnNode(Opcodes.DADD))
                                offsetPlayerPosition.add(new VarInsnNode(Opcodes.DSTORE, 5)) // d0 D

                                offsetPlayerPosition.add(new InsnNode(Opcodes.DUP))
                                offsetPlayerPosition.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorY",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetPlayerPosition.add(new VarInsnNode(Opcodes.DLOAD, 7)) // d1 D
                                offsetPlayerPosition.add(new InsnNode(Opcodes.DADD))
                                offsetPlayerPosition.add(new VarInsnNode(Opcodes.DSTORE, 7)) // d1 D

                                offsetPlayerPosition.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorZ",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetPlayerPosition.add(new VarInsnNode(Opcodes.DLOAD, 9)) // d2 D
                                offsetPlayerPosition.add(new InsnNode(Opcodes.DADD))
                                offsetPlayerPosition.add(new VarInsnNode(Opcodes.DSTORE, 9)) // d2 D


                                // ...
                                // INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getZ ()I
                                // I2D
                                // LDC 0.5
                                // DADD
                                // DSUB
                                // DSTORE 9
                                oldInstructions.insert(oldInstructions.get(i+6), offsetPlayerPosition)
                                // ...

                                logTransformSuccess("function func_225416_a", "net.minecraft.server.management.PlayerInteractionManager")
                                success = true

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function func_225416_a", "net.minecraft.server.management.PlayerInteractionManager", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function func_225416_a", "net.minecraft.server.management.PlayerInteractionManager", exception.message)
                    }

                } else
                    logTransformError("function func_225416_a", "net.minecraft.server.management.PlayerInteractionManager", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Allows players to interact with blocks relative to their offsets on a server.
         * Allows players to interact with entities relative to their offsets on a server.
         */
        "ServerPlayNetHandler": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.network.play.ServerPlayNetHandler"
            },

            "transformer": function(classNode) {
                // Allows players to interact with blocks relative to their offsets.
                var processTryUseItemOnBlock = findObfuscatedMethodWithSignature(classNode, "processTryUseItemOnBlock", "func_184337_a",
                    "(Lnet/minecraft/network/play/client/CPlayerTryUseItemOnBlockPacket;)V")

                if (processTryUseItemOnBlock !== null) {
                    try {
                        var oldInstructions = processTryUseItemOnBlock.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/ServerPlayerEntity", "getDistanceSq",
                                    "func_70092_e", "(DDD)D")) {
                                var redoGetDistanceSq = new InsnList()
                                var skipOriginal = new LabelNode()

                                redoGetDistanceSq.add(skipOriginal)
                                redoGetDistanceSq.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;DDD)D",
                                    false
                                ))

                                // ...
                                redoGetDistanceSq.insertBefore(instruction, new JumpInsnNode(Opcodes.GOTO, skipOriginal))
                                // INVOKEVIRTUAL net/minecraft/entity/player/ServerPlayerEntity.getDistanceSq (DDD)D
                                redoGetDistanceSq.insert(instruction, redoGetDistanceSq)
                                // ...

                                success = true
                                logTransformSuccess("function processTryUseItemOnBlock", "net.minecraft.network.play.ServerPlayNetHandler")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function processTryUseItemOnBlock", "net.minecraft.network.play.ServerPlayNetHandler", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function processTryUseItemOnBlock", "net.minecraft.network.play.ServerPlayNetHandler", exception.message)
                    }

                } else
                    logTransformError("function processTryUseItemOnBlock", "net.minecraft.network.play.ServerPlayNetHandler", "Unable to find function to transform")

                // Allows players to interact with entities relative to their offsets.
                var processUseEntity = findObfuscatedMethodWithSignature(classNode, "processUseEntity", "func_147340_a",
                                    "(Lnet/minecraft/network/play/client/CUseEntityPacket;)V")

                if (processUseEntity !== null) {
                    try {
                        var oldInstructions = processUseEntity.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/ServerPlayerEntity", "getDistanceSq",
                                    "func_70068_e", "(Lnet/minecraft/entity/Entity;)D")) {
                                var redoGetDistanceSq = new InsnList()
                                var skipOriginal = new LabelNode()

                                redoGetDistanceSq.add(skipOriginal)
                                redoGetDistanceSq.add(new InsnNode(Opcodes.SWAP))
                                redoGetDistanceSq.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;)D",
                                    false
                                ))

                                // ...
                                oldInstructions.insertBefore(instruction, new JumpInsnNode(Opcodes.GOTO, skipOriginal))
                                // INVOKEVIRTUAL net/minecraft/entity/player/ServerPlayerEntity.getDistanceSq (Lnet/minecraft/entity/Entity;)D
                                oldInstructions.insert(instruction, redoGetDistanceSq)
                                // ...

                                success = true
                                logTransformSuccess("function processUseEntity", "net.minecraft.network.play.ServerPlayNetHandler")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function processUseEntity", "net.minecraft.network.play.ServerPlayNetHandler", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function processUseEntity", "net.minecraft.network.play.ServerPlayNetHandler", exception.message)
                    }

                } else
                    logTransformError("function processUseEntity", "net.minecraft.network.play.ServerPlayNetHandler", "Unable to find function to transform")

                return classNode
            }
        },



        /**
         * Adds an offset to the raycast that finds what entity the player is looking at.
         * Allows players to interact with entities relative to their offsets.
         */
        "GameRenderer": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.client.renderer.GameRenderer"
            },

            "transformer": function(classNode) {
                var getMouseOver = findObfuscatedMethodWithSignature(classNode, "getMouseOver", "func_78473_a", "(F)V")

                if (getMouseOver !== null) {
                    var oldInstructions = getMouseOver.instructions

                    // Adds an offset to the raycast that finds what entity the player is looking at.
                    try {
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            if (checkObfuscatedMethodInsn(oldInstructions.get(i), Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEyePosition", "func_174824_e",
                                    "(F)Lnet/minecraft/util/math/Vec3d;")) {
                                var offsetRaycastInstructions = new InsnList()

                                offsetRaycastInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2)) // entity Lnet/minecraft/entity/Entity;
                                offsetRaycastInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "offsetVector",
                                    "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))

                                // ...
                                // INVOKEVIRTUAL net/minecraft/entity/Entity.getEyePosition (F)Lnet/minecraft/util/math/Vec3d;
                                oldInstructions.insert(oldInstructions.get(i), offsetRaycastInstructions)
                                // ...

                                success = true
                                logTransformSuccess("first area of function getMouseOver", "net.minecraft.client.renderer.GameRenderer")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function getMouseOver", "net.minecraft.client.renderer.GameRenderer", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("first area of function getMouseOver", "net.minecraft.client.renderer.GameRenderer", exception.message)
                    }

                    // Allows players to interact with entities relative to their offsets.
                    try {
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            if (checkObfuscatedMethodInsn(oldInstructions.get(i), Opcodes.INVOKEVIRTUAL, "net/minecraft/util/math/AxisAlignedBB", "grow", "func_72314_b",
                                    "(DDD)Lnet/minecraft/util/math/AxisAlignedBB;")) {
                                var offsetAABBInstructions = new InsnList()

                                offsetAABBInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2)) // entity Lnet/minecraft/entity/Entity;
                                offsetAABBInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "offsetAxisAlignedBB",
                                    "(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/AxisAlignedBB;",
                                    false
                                ))

                                // ...
                                // INVOKEVIRTUAL net/minecraft/util/math/AxisAlignedBB.grow (DDD)Lnet/minecraft/util/math/AxisAlignedBB;
                                oldInstructions.insert(oldInstructions.get(i), offsetAABBInstructions)
                                // ...

                                success = true
                                logTransformSuccess("second area of function getMouseOver", "net.minecraft.client.renderer.GameRenderer")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function getMouseOver", "net.minecraft.client.renderer.GameRenderer", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("second area of function getMouseOver", "net.minecraft.client.renderer.GameRenderer", exception.message)
                    }

                } else
                    logTransformError("function getMouseOver", "net.minecraft.client.renderer.GameRenderer", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Forces players to render their third-person model in first-person if they have an offset.
         */
        "WorldRenderer": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.client.renderer.WorldRenderer"
            },

            "transformer": function(classNode) {
                var updateCameraAndRender = findObfuscatedMethodWithSignature(classNode, "updateCameraAndRender", "func_228426_a_",
                    "(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/renderer/Matrix4f;)V")

                if (updateCameraAndRender !== null) {
                    try {
                        var oldInstructions = updateCameraAndRender.instructions
                        var success = false

                        for (var i = 0; i <= oldInstructions.size() - 2; i++) {
                            if (checkObfuscatedMethodInsn(oldInstructions.get(i), Opcodes.INVOKEVIRTUAL, "net/minecraft/client/renderer/ActiveRenderInfo", "isThirdPerson", "func_216770_i", "()Z")
                                    && checkJumpInsn(oldInstructions.get(i+1), Opcodes.IFNE)) {
                                var overrideIsThirdPerson = new InsnList()

                                overrideIsThirdPerson.add(new VarInsnNode(Opcodes.ALOAD, 6)) // activeRenderInfoIn Lnet/minecraft/client/renderer/ActiveRenderInfo;
                                overrideIsThirdPerson.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "modifiedIsThirdPerson",
                                    "(Lnet/minecraft/client/renderer/ActiveRenderInfo;)Z",
                                    false
                                ))
                                overrideIsThirdPerson.add(new JumpInsnNode(Opcodes.IFNE, oldInstructions.get(i+1).label))

                                // ...
                                // INVOKEVIRTUAL net/minecraft/client/renderer/ActiveRenderInfo.isThirdPerson ()Z
                                // IFNE L83
                                oldInstructions.insert(oldInstructions.get(i+1), overrideIsThirdPerson)
                                // ...

                                success = true
                                logTransformSuccess("function updateCameraAndRender", "net.minecraft.client.renderer.WorldRenderer")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function updateCameraAndRender", "net.minecraft.client.renderer.WorldRenderer", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function updateCameraAndRender", "net.minecraft.client.renderer.WorldRenderer", exception.message)
                    }

                } else
                    logTransformError("function updateCameraAndRender", "net.minecraft.client.renderer.WorldRenderer", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Prevents a player from rendering their first-person hand if they have an offset.
         */
        "FirstPersonRenderer": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.client.renderer.FirstPersonRenderer"
            },

            "transformer": function(classNode) {
                var renderItemInFirstPerson = findObfuscatedMethodWithSignature(classNode, "renderItemInFirstPerson", "func_228396_a_",
                    "(FLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;Lnet/minecraft/client/entity/player/ClientPlayerEntity;I)V")

                if (renderItemInFirstPerson !== null) {
                    try {
                        var testSkipRenderFirstPerson = new InsnList()
                        var skipReturn = new LabelNode()

                        testSkipRenderFirstPerson.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                            "shouldRenderHand",
                            "()Z",
                            false
                        ))
                        testSkipRenderFirstPerson.add(new JumpInsnNode(Opcodes.IFNE, skipReturn))
                            testSkipRenderFirstPerson.add(new InsnNode(Opcodes.RETURN))
                        testSkipRenderFirstPerson.add(skipReturn)

                        // METHOD START.
                        renderItemInFirstPerson.instructions.insert(testSkipRenderFirstPerson)
                        // ...

                        logTransformSuccess("function renderItemInFirstPerson", "net.minecraft.client.renderer.FirstPersonRenderer")

                    } catch (exception) {
                        logTransformError("function renderItemInFirstPerson", "net.minecraft.client.renderer.FirstPersonRenderer", exception.message)
                    }

                } else
                    logTransformError("function renderItemInFirstPerson", "net.minecraft.client.renderer.FirstPersonRenderer", "Unable to find function to transform")

                return classNode
            }
        },


        /**
         * Checks if the player is in range to render both with the normal and offset positions.
         * Runs the frustum check twice so it can account for players' offsets, allowing the offset position to be rendered even when the original position is not in view.
         */
        "EntityRenderer": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.client.renderer.entity.EntityRenderer"
            },

            "transformer": function(classNode) {
                var shouldRender = findObfuscatedMethodWithSignature(classNode, "shouldRender", "func_225626_a_",
                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ClippingHelperImpl;DDD)Z")

                if (shouldRender !== null) {
                    var oldInstructions = shouldRender.instructions

                    // Checks if the player is in range to render both with the normal and offset positions.
                    try {
                        var success = false

                        for (var i = 0; i <= oldInstructions.size() - 2; i++) {
                            if (checkObfuscatedMethodInsn(oldInstructions.get(i), Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "isInRangeToRender3d", "func_145770_h", "(DDD)Z")
                                    && checkJumpInsn(oldInstructions.get(i+1), Opcodes.IFNE)) {
                                var redoIsInRange = new InsnList()

                                redoIsInRange.add(new VarInsnNode(Opcodes.ALOAD, 1)) // livingEntityIn Lnet/minecraft/entity/Entity;
                                redoIsInRange.add(new VarInsnNode(Opcodes.DLOAD, 3)) // camX D
                                redoIsInRange.add(new VarInsnNode(Opcodes.DLOAD, 5)) // camY D
                                redoIsInRange.add(new VarInsnNode(Opcodes.DLOAD, 7)) // camZ D
                                redoIsInRange.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "modifiedIsInRangeToRender3d",
                                    "(Lnet/minecraft/entity/Entity;DDD)Z",
                                    false
                                ))
                                redoIsInRange.add(new JumpInsnNode(Opcodes.IFNE, oldInstructions.get(i+1).label))

                                // ...
                                // INVOKEVIRTUAL net/minecraft/entity/Entity.isInRangeToRender3d (DDD)Z
                                // IFNE L1
                                oldInstructions.insert(oldInstructions.get(i+1), redoIsInRange)
                                // ...

                                success = true
                                logTransformSuccess("first area of function shouldRender", "net.minecraft.client.renderer.entity.EntityRenderer")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("first area of function shouldRender", "net.minecraft.client.renderer.entity.EntityRenderer", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("first area of function shouldRender", "net.minecraft.client.renderer.entity.EntityRenderer", exception.message)
                    }

                    // Runs the frustum check twice so it can account for players' offsets, allowing the offset position to be rendered even when the original position is not in view.
                    try {
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/client/renderer/culling/ClippingHelperImpl",
                                    "isBoundingBoxInFrustum", "func_228957_a_", "(Lnet/minecraft/util/math/AxisAlignedBB;)Z")) {
                                var doubleCheckFrustum = new InsnList()
                                var skipToReturn = new LabelNode()

                                // Returns if the entity should be rendered.
                                doubleCheckFrustum.add(new InsnNode(Opcodes.DUP))
                                doubleCheckFrustum.add(new JumpInsnNode(Opcodes.IFNE, skipToReturn))
                                    doubleCheckFrustum.add(new VarInsnNode(Opcodes.ALOAD, 1)) // livingEntityIn Lnet/minecraft/entity/Entity;
                                    doubleCheckFrustum.add(new VarInsnNode(Opcodes.ALOAD, 2)) // camera Lnet/minecraft/client/renderer/culling/ClippingHelperImpl;
                                    doubleCheckFrustum.add(new VarInsnNode(Opcodes.ALOAD, 9)) // axisalignedbb Lnet/minecraft/util/math/AxisAlignedBB;
                                    doubleCheckFrustum.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "modifiedIsBoundingBoxInFrustum",
                                        "(ZLnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ClippingHelperImpl;Lnet/minecraft/util/math/AxisAlignedBB;)Z",
                                        false
                                    ))
                                doubleCheckFrustum.add(skipToReturn)

                                // ...
                                // INVOKEVIRTUAL net/minecraft/client/renderer/culling/ClippingHelperImpl.isBoundingBoxInFrustum (Lnet/minecraft/util/math/AxisAlignedBB;)Z
                                oldInstructions.insert(instruction, doubleCheckFrustum)
                                // ...

                                success = true
                                logTransformSuccess("second area of function shouldRender", "net.minecraft.client.renderer.entity.EntityRenderer")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("second area of function shouldRender", "net.minecraft.client.renderer.entity.EntityRenderer", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("second area of function shouldRender", "net.minecraft.client.renderer.entity.EntityRenderer", exception.message)
                    }

                } else
                    logTransformError("function shouldRender", "net.minecraft.client.renderer.entity.EntityRenderer", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Modifies the rendering of leashes.
         */
        "MobRenderer": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.client.renderer.entity.MobRenderer"
            },

            "transformer": function(classNode) {
                var renderLeash = findObfuscatedMethodWithSignature(classNode, "renderLeash", "func_229118_a_",
                    "(Lnet/minecraft/entity/MobEntity;FLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;Lnet/minecraft/entity/Entity;)V")

                if (renderLeash !== null) {
                    try {
                        var oldInstructions = renderLeash.instructions
                        var success = false

                        for (var i = 0; i <= oldInstructions.size() - 4; i++) {
                            if (checkVarInsn(oldInstructions.get(i), Opcodes.DLOAD, 16) && checkInsn(oldInstructions.get(i+1), Opcodes.DMUL) && checkInsn(oldInstructions.get(i+2), Opcodes.DADD)
                                    && checkVarInsn(oldInstructions.get(i+3), Opcodes.DSTORE, 22)) {
                                var offsetLeashRender = new InsnList()


                                offsetLeashRender.add(new VarInsnNode(Opcodes.ALOAD, 5))
                                offsetLeashRender.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "getOffsets",
                                    "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))

                                offsetLeashRender.add(new InsnNode(Opcodes.DUP))
                                offsetLeashRender.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorX",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetLeashRender.add(new VarInsnNode(Opcodes.DLOAD, 18)) // d6 D
                                offsetLeashRender.add(new InsnNode(Opcodes.DADD))
                                offsetLeashRender.add(new VarInsnNode(Opcodes.DSTORE, 18)) // d6 D

                                offsetLeashRender.add(new InsnNode(Opcodes.DUP))
                                offsetLeashRender.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorY",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetLeashRender.add(new VarInsnNode(Opcodes.DLOAD, 20)) // d7 D
                                offsetLeashRender.add(new InsnNode(Opcodes.DADD))
                                offsetLeashRender.add(new VarInsnNode(Opcodes.DSTORE, 20)) // d7 D

                                offsetLeashRender.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorZ",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                offsetLeashRender.add(new VarInsnNode(Opcodes.DLOAD, 22)) // d8 D
                                offsetLeashRender.add(new InsnNode(Opcodes.DADD))
                                offsetLeashRender.add(new VarInsnNode(Opcodes.DSTORE, 22)) // d8 D


                                // ...
                                // DLOAD 16
                                // DMUL
                                // DADD
                                // DSTORE 22
                                oldInstructions.insert(oldInstructions.get(i+3), offsetLeashRender)
                                // ...

                                success = true
                                logTransformSuccess("function renderLeash", "net.minecraft.client.renderer.entity.MobRenderer")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function renderLeash", "net.minecraft.client.renderer.entity.MobRenderer", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function renderLeash", "net.minecraft.client.renderer.entity.MobRenderer", exception.message)
                    }

                } else
                    logTransformError("function renderLeash", "net.minecraft.client.renderer.entity.MobRenderer", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Forces fishing lines to always render as if the player was in third-person if the player has offsets.
         * Offsets rendered fishing lines.
         */
        "FishRenderer": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.client.renderer.entity.FishRenderer"
            },

            "transformer": function(classNode) {
                var render = findObfuscatedMethodWithSignature(classNode, "render", "func_225623_a_",
                    "(Lnet/minecraft/entity/projectile/FishingBobberEntity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V")

                if (render !== null) {
                    var oldInstructions = render.instructions

                    // Forces third-person fishing line rendering when player is offset.
                    try {
                        var success = false

                        for (var i = 0; i <= oldInstructions.size() - 9; i++) {
                            if (checkVarInsn(oldInstructions.get(i), Opcodes.ALOAD, 0) && checkObfuscatedFieldInsn(oldInstructions.get(i+1), Opcodes.GETFIELD, "net/minecraft/client/renderer/entity/FishRenderer", "renderManager", "field_76990_c", "Lnet/minecraft/client/renderer/entity/EntityRendererManager;")
                                    && checkObfuscatedFieldInsn(oldInstructions.get(i+2), Opcodes.GETFIELD, "net/minecraft/client/renderer/entity/EntityRendererManager", "options", "field_78733_k", "Lnet/minecraft/client/GameSettings;")
                                    && checkInsn(oldInstructions.get(i+3), Opcodes.IFNULL)

                                    && checkVarInsn(oldInstructions.get(i+4), Opcodes.ALOAD, 0) && checkObfuscatedFieldInsn(oldInstructions.get(i+5), Opcodes.GETFIELD, "net/minecraft/client/renderer/entity/FishRenderer", "renderManager", "field_76990_c", "Lnet/minecraft/client/renderer/entity/EntityRendererManager;")
                                    && checkObfuscatedFieldInsn(oldInstructions.get(i+6), Opcodes.GETFIELD, "net/minecraft/client/renderer/entity/EntityRendererManager", "options", "field_78733_k", "Lnet/minecraft/client/GameSettings;")
                                    && checkObfuscatedFieldInsn(oldInstructions.get(i+7), Opcodes.GETFIELD, "net/minecraft/client/GameSettings", "thirdPersonView", "field_74320_O", "I")

                                    && checkJumpInsn(oldInstructions.get(i+8), Opcodes.IFGT)) {
                                var firstInstruction = oldInstructions.get(i)
                                var lastInstruction = oldInstructions.get(i+8)
                                var render3dIfHasOffsets = new InsnList()
                                var skipOriginal = new LabelNode()
                                var runOriginal = new LabelNode()
                                var skipRender3dIfNoOffsets = new LabelNode()

                                // Skips inserted code after returning to and running original code.
                                render3dIfHasOffsets.add(new JumpInsnNode(Opcodes.GOTO, skipRender3dIfNoOffsets))
                                // Skips (this.renderManager.options == null || this.renderManager.options.thirdPersonView <= 0).
                                render3dIfHasOffsets.add(skipOriginal)
                                render3dIfHasOffsets.add(new VarInsnNode(Opcodes.ALOAD, 7)) // playerentity Lnet/minecraft/entity/player/PlayerEntity;
                                render3dIfHasOffsets.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "isPlayerOffset",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;)Z",
                                    false
                                ))
                                // Goes to third-person rendering if the player has offsets, else it returns to the original code.
                                render3dIfHasOffsets.add(new JumpInsnNode(Opcodes.IFEQ, lastInstruction.label))
                                render3dIfHasOffsets.add(new JumpInsnNode(Opcodes.GOTO, runOriginal))
                                render3dIfHasOffsets.add(skipRender3dIfNoOffsets)

                                // ...
                                oldInstructions.insertBefore(firstInstruction, new JumpInsnNode(Opcodes.GOTO, skipOriginal))
                                oldInstructions.insertBefore(firstInstruction, runOriginal)
                                // ALOAD 0
                                // GETFIELD net/minecraft/client/renderer/entity/FishRenderer.renderManager : Lnet/minecraft/client/renderer/entity/EntityRendererManager;
                                // GETFIELD net/minecraft/client/renderer/entity/EntityRendererManager.options : Lnet/minecraft/client/GameSettings;
                                // IFNULL L31
                                // ALOAD 0
                                // GETFIELD net/minecraft/client/renderer/entity/FishRenderer.renderManager : Lnet/minecraft/client/renderer/entity/EntityRendererManager;
                                // GETFIELD net/minecraft/client/renderer/entity/EntityRendererManager.options : Lnet/minecraft/client/GameSettings;
                                // GETFIELD net/minecraft/client/GameSettings.thirdPersonView : I
                                oldInstructions.insert(lastInstruction, render3dIfHasOffsets)
                                // ...

                                success = true
                                logTransformSuccess("first area of function render", "net.minecraft.client.renderer.FishRenderer")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("first area of function render", "net.minecraft.client.renderer.FishRenderer", "Unable to find injection point")

                    } catch (expression) {
                        logTransformError("first area of function render", "net.minecraft.client.renderer.FishRenderer", exception.message)
                    }

                    // Puts in offset to fishing line position.
                    try {
                        var success = false

                        for (var i = 0; i <= oldInstructions.size() - 2; i++) {
                            if (checkVarInsn(oldInstructions.get(i), Opcodes.ALOAD, 7) && checkObfuscatedMethodInsn(oldInstructions.get(i+1), Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "isCrouching", "func_213453_ef", "()Z")) {
                                var addOffsetsWhen3d = new InsnList()


                                addOffsetsWhen3d.add(new VarInsnNode(Opcodes.ALOAD, 7)) // playerentity Lnet/minecraft/entity/player/PlayerEntity;
                                addOffsetsWhen3d.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "getOffsets",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))

                                addOffsetsWhen3d.add(new InsnNode(Opcodes.DUP))
                                addOffsetsWhen3d.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorX",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                addOffsetsWhen3d.add(new VarInsnNode(Opcodes.DLOAD, 25)) // d4 D
                                addOffsetsWhen3d.add(new InsnNode(Opcodes.DADD))
                                addOffsetsWhen3d.add(new VarInsnNode(Opcodes.DSTORE, 25)) // d4 D

                                addOffsetsWhen3d.add(new InsnNode(Opcodes.DUP))
                                addOffsetsWhen3d.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorY",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                addOffsetsWhen3d.add(new VarInsnNode(Opcodes.DLOAD, 27)) // d5 D
                                addOffsetsWhen3d.add(new InsnNode(Opcodes.DADD))
                                addOffsetsWhen3d.add(new VarInsnNode(Opcodes.DSTORE, 27)) // d5 D

                                addOffsetsWhen3d.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorZ",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                addOffsetsWhen3d.add(new VarInsnNode(Opcodes.DLOAD, 29)) // d6 D
                                addOffsetsWhen3d.add(new InsnNode(Opcodes.DADD))
                                addOffsetsWhen3d.add(new VarInsnNode(Opcodes.DSTORE, 29)) // d6 D


                                // ...
                                oldInstructions.insertBefore(oldInstructions.get(i), addOffsetsWhen3d)
                                // ALOAD 7
                                // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.isCrouching ()Z
                                // ...

                                success = true
                                logTransformSuccess("second area of function render", "net.minecraft.client.renderer.FishRenderer")

                                break
                            }
                        }

                        if (!success)
                            logTransformError("second area of function render", "net.minecraft.client.renderer.FishRenderer", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("second area of function render", "net.minecraft.client.renderer.FishRenderer", exception.message)
                    }

                } else
                    logTransformError("function render", "net.minecraft.client.renderer.FishRenderer", "Unable to find function to transform")

                return classNode
            }
        }
    }
}