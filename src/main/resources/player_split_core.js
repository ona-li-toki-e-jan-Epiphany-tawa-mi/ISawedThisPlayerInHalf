var Opcodes = Java.type("org.objectweb.asm.Opcodes")
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode")
var InsnList = Java.type("org.objectweb.asm.tree.InsnList")
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode")
var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode")
var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode")
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode")
var TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode")
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode")



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
 * @param {string} descriptor The descriptor the instruction should have.
 */
function checkFieldInsn(instructionNode, opCode, owner, name, descriptor) {
    return instructionNode.getOpcode() === opCode && instructionNode.owner === owner && instructionNode.name === name &&
        instructionNode.desc === descriptor
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
 * Checks if a instruction node has the given opcode.
 *
 * @param {object/InsnNode} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 */
function checkInsn(instructionNode, opCode) {
    return instructionNode.getOpcode() === opCode
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
 * @param {object/MethodNode} instructionNode The instruction node to check.
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
 * Checks if a type instruction node has the given opcode and index.
 *
 * @param {object/TypeInsnNode} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 * @param {string} descriptor The descriptor that the instruction should have.
 */
function checkTypeInsn(instructionNode, opCode, descriptor) {
    return instructionNode.getOpcode() === opCode && instructionNode.desc === descriptor
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
    print("[" + currentDate.getHours() + ":" + currentDate.getMinutes() + ":" + currentDate.getSeconds() + "] [PlayerSplitCore/" +
        loggingLevel.numericLevel + "]: " + message)
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
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEyePosition", "func_174824_e",
                                    "(F)Lnet/minecraft/util/math/Vec3d;")) {
                                var newInstructions = new InsnList()

                                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
                                newInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "offsetRaycast",
                                    "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))

                                // ...
                                // INVOKEVIRTUAL net/minecraft/entity/Entity.getEyePosition (F)Lnet/minecraft/util/math/Vec3d;
                                oldInstructions.insert(instruction, newInstructions)
                                // ...

                                logTransformSuccess("function pick", "net.minecraft.entity.Entity")
                                success = true

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
         * Adds an offset to the raycast that finds what entity the player is looking at.
         */
        "GameRenderer": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.client.renderer.GameRenderer"
            },

            "transformer": function(classNode) {
                var getMouseOver = findObfuscatedMethodWithSignature(classNode, "getMouseOver", "func_78473_a", "(F)V")

                if (getMouseOver !== null) {
                    try {
                        var oldInstructions = getMouseOver.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEyePosition", "func_174824_e",
                                    "(F)Lnet/minecraft/util/math/Vec3d;")) {
                                var newInstructions = new InsnList()

                                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2))
                                newInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "offsetRaycast",
                                    "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))

                                // ...
                                // INVOKEVIRTUAL net/minecraft/entity/Entity.getEyePosition (F)Lnet/minecraft/util/math/Vec3d;
                                oldInstructions.insert(instruction, newInstructions)
                                // ...

                                logTransformSuccess("function getMouseOver", "net.minecraft.client.renderer.GameRenderer")
                                success = true

                                break
                            }
                        }

                        if (!success)
                            logTransformError("function getMouseOver", "net.minecraft.client.renderer.GameRenderer", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function getMouseOver", "net.minecraft.client.renderer.GameRenderer", exception.message)
                    }

                } else
                    logTransformError("function getMouseOver", "net.minecraft.client.renderer.GameRenderer", "Unable to find function to transform")

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
                        var newInstructions = new InsnList()
                        var skipReturn = new LabelNode()


                        newInstructions.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "com/epiphany/isawedthisplayerinhalf/rendering/RenderingOffsetter",
                            "shouldRenderHand",
                            "()Z",
                            false
                        ))
                        newInstructions.add(new JumpInsnNode(Opcodes.IFNE, skipReturn))
                            newInstructions.add(new InsnNode(Opcodes.RETURN))
                        newInstructions.add(skipReturn)


                        renderItemInFirstPerson.instructions.insert(newInstructions)
                        //...

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

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/client/renderer/ActiveRenderInfo", "isThirdPerson",
                                    "func_216770_i", "()Z")) {
                                oldInstructions.insert(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/rendering/RenderingOffsetter",
                                    "modifiedIsThirdPerson",
                                    "(Lnet/minecraft/client/renderer/ActiveRenderInfo;)Z",
                                    false
                                ))
                                oldInstructions.remove(instruction)

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
                    var oldInstructions = methodNode.instructions
                    var success = false

                    for (var i = 0; i < oldInstructions.size(); i++) {
                        var instruction = oldInstructions.get(i)

                        if (checkMethodInsn(instruction, Opcodes.INVOKESPECIAL, "net/minecraft/entity/projectile/AbstractArrowEntity", "<init>",
                                "(Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V")) {
                            var newInstructions = new InsnList()

                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2))
                            newInstructions.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                "offsetProjectile",
                                "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;)V",
                                false
                            ))

                            // ...
                            // INVOKESPECIAL net/minecraft/entity/projectile/AbstractArrowEntity.<init> (Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V
                            oldInstructions.insert(instruction, newInstructions)
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

                return methodNode
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
                    var oldInstructions = methodNode.instructions
                    var success = false

                    for (var i = 0; i < oldInstructions.size(); i++) {
                        var instruction = oldInstructions.get(i)

                        if (checkMethodInsn(instruction, Opcodes.INVOKESPECIAL, "net/minecraft/entity/projectile/ThrowableEntity", "<init>",
                                "(Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V")) {
                            var newInstructions = new InsnList()

                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0))
                            newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2))
                            newInstructions.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                "offsetProjectile",
                                "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;)V",
                                false
                            ))

                            // ...
                            // INVOKESPECIAL net/minecraft/entity/projectile/ThrowableEntity.<init> (Lnet/minecraft/entity/EntityType;DDDLnet/minecraft/world/World;)V
                            oldInstructions.insert(instruction, newInstructions)
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

                return methodNode
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
                            var instruction = oldInstructions.get(i)

                            if (checkMethodInsn(instruction, Opcodes.INVOKESPECIAL, "net/minecraft/entity/item/EyeOfEnderEntity", "<init>",
                                    "(Lnet/minecraft/world/World;DDD)V")) {
                                var newInstructions = new InsnList()

                                newInstructions.add(new InsnNode(Opcodes.DUP))
                                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2))
                                newInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "offsetProjectile",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;)V",
                                    false
                                ))

                                // ...
                                // INVOKESPECIAL net/minecraft/entity/item/EyeOfEnderEntity.<init> (Lnet/minecraft/world/World;DDD)V
                                oldInstructions.insert(instruction, newInstructions)
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
                            var instruction = oldInstructions.get(i)

                            if (checkMethodInsn(instruction, Opcodes.INVOKESPECIAL, "net/minecraft/entity/projectile/FishingBobberEntity", "<init>",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;II)V")) {
                                var newInstructions = new InsnList()

                                newInstructions.add(new InsnNode(Opcodes.DUP))
                                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2))
                                newInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "offsetProjectile",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;)V",
                                    false
                                ))

                                // ...
                                // INVOKESPECIAL net/minecraft/entity/projectile/FishingBobberEntity.<init> (Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;II)V
                                oldInstructions.insert(instruction, newInstructions)
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
         * Offsets the fishing line, and forces it to always render if it was in third-person.
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
                    try {
                        var oldInstructions = render.instructions
                        var successes = 0

                        // Forces third-person fishing line rendering.
                        for (var i = 0; i < oldInstructions.size() - 9; i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkVarInsn(instruction, Opcodes.ALOAD, 0)) {
                                var instructions = [instruction]

                                for (var k = 1; k < 9; k++)
                                    instructions.push(oldInstructions.get(i + k))

                                if (checkObfuscatedFieldInsn(instructions[1], Opcodes.GETFIELD, "net/minecraft/client/renderer/entity/FishRenderer", "renderManager", "field_76990_c", "Lnet/minecraft/client/renderer/entity/EntityRendererManager;")
                                        && checkObfuscatedFieldInsn(instructions[2], Opcodes.GETFIELD, "net/minecraft/client/renderer/entity/EntityRendererManager", "options", "field_78733_k", "Lnet/minecraft/client/GameSettings;")
                                        && checkInsn(instructions[3], Opcodes.IFNULL)

                                        && checkVarInsn(instructions[4], Opcodes.ALOAD, 0) && checkObfuscatedFieldInsn(instructions[5], Opcodes.GETFIELD, "net/minecraft/client/renderer/entity/FishRenderer", "renderManager", "field_76990_c", "Lnet/minecraft/client/renderer/entity/EntityRendererManager;")
                                        && checkObfuscatedFieldInsn(instructions[6], Opcodes.GETFIELD, "net/minecraft/client/renderer/entity/EntityRendererManager", "options", "field_78733_k", "Lnet/minecraft/client/GameSettings;")
                                        && checkObfuscatedFieldInsn(instructions[7], Opcodes.GETFIELD, "net/minecraft/client/GameSettings", "thirdPersonView", "field_74320_O", "I") && checkInsn(instructions[8], Opcodes.IFGT)) {
                                    oldInstructions.insertBefore(instructions[0], new JumpInsnNode(Opcodes.GOTO, instructions[8].label))

                                    for (var k = 0; k < instructions.length; k++)
                                        oldInstructions.remove(instructions[k])

                                    successes |= 1
                                    break
                                }
                            }
                        }

                        if (successes & 1 === 0)
                            logTransformError("function render", "net.minecraft.client.renderer.FishRenderer", "Unable to find primary injection points")

                        // Puts in offset to fishing line position.
                        for (var i = 0; i < oldInstructions.size() - 21; i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkVarInsn(instruction, Opcodes.DLOAD, 25)) {
                                var instructions = [instruction]

                                for (var k = 1; k < 21; k++) {
                                    var potentialInstruction = oldInstructions.get(i + k)

                                    if (potentialInstruction.getOpcode() !== -1)
                                        instructions.push(potentialInstruction)
                                }

                                if (checkVarInsn(instructions[1], Opcodes.DLOAD, 32) && checkInsn(instructions[2], Opcodes.DSUB) && checkInsn(instructions[3], Opcodes.D2F)
                                        && checkVarInsn(instructions[4], Opcodes.FSTORE, 38)

                                        && checkVarInsn(instructions[5], Opcodes.DLOAD, 27) && checkVarInsn(instructions[6], Opcodes.DLOAD, 34) && checkInsn(instructions[7], Opcodes.DSUB) && checkInsn(instructions[8], Opcodes.D2F)
                                        && checkVarInsn(instructions[9], Opcodes.FLOAD, 31) && checkInsn(instructions[10], Opcodes.FADD) && checkVarInsn(instructions[11], Opcodes.FSTORE, 39)

                                        && checkVarInsn(instructions[12], Opcodes.DLOAD, 29) && checkVarInsn(instructions[13], Opcodes.DLOAD, 36) && checkInsn(instructions[14], Opcodes.DSUB) && checkInsn(instructions[15], Opcodes.D2F)
                                        && checkVarInsn(instructions[16], Opcodes.FSTORE, 40)) {
                                    var getVectorList = new InsnList()
                                    var addXList = new InsnList()
                                    var addYList = new InsnList()
                                    var addZList = new InsnList()


                                    getVectorList.add(new VarInsnNode(Opcodes.ALOAD, 7))
                                    getVectorList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                        "getOffsets",
                                        "(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/math/Vec3d;",
                                        false
                                    ))
                                    getVectorList.add(new InsnNode(Opcodes.DUP))
                                    getVectorList.add(new InsnNode(Opcodes.DUP))

                                    addXList.add(new InsnNode(Opcodes.DUP2_X1))
                                    addXList.add(new InsnNode(Opcodes.POP2))
                                    addXList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorX",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addXList.add(new InsnNode(Opcodes.DADD))

                                    addYList.add(new InsnNode(Opcodes.DUP2_X1))
                                    addYList.add(new InsnNode(Opcodes.POP2))
                                    addYList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorY",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addYList.add(new InsnNode(Opcodes.DADD))

                                    addZList.add(new InsnNode(Opcodes.DUP2_X1))
                                    addZList.add(new InsnNode(Opcodes.POP2))
                                    addZList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorZ",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addZList.add(new InsnNode(Opcodes.DADD))


                                    // ...
                                    oldInstructions.insertBefore(instructions[0], getVectorList)
                                    // DLOAD 25
                                    // DLOAD 32
                                    // DSUB
                                    oldInstructions.insert(instructions[2], addXList)
                                    // D2F
                                    // FSTORE 38
                                    // DLOAD 27
                                    // DLOAD 34
                                    // DSUB
                                    oldInstructions.insert(instructions[7], addYList)
                                    // D2F
                                    // FLOAD 31
                                    // FADD
                                    // FSTORE 39
                                    // DLOAD 29
                                    // DLOAD 36
                                    // DSUB
                                    oldInstructions.insert(instructions[14], addZList)
                                    // D2F
                                    // FSTORE 40
                                    // ...

                                    successes |= 2
                                    break
                                }
                            }
                        }

                        if (successes & 2 === 0) {
                            logTransformError("function render", "net.minecraft.client.renderer.FishRenderer", "Unable to find secondary injection points")

                        } else if (successes & 1 === 1)
                            logTransformSuccess("function render", "net.minecraft.client.renderer.FishRenderer")

                    } catch (exception) {
                        logTransformError("function render", "net.minecraft.client.renderer.FishRenderer", exception.message)
                    }

                } else
                    logTransformError("function render", "net.minecraft.client.renderer.FishRenderer", "Unable to find function to transform")

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
                                oldInstructions.insert(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/player/PlayerEntity;)D",
                                    false
                                ))
                                oldInstructions.remove(instruction)

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

                        for (var i = 0; i < oldInstructions.size() - 25; i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkVarInsn(instruction, Opcodes.ALOAD, 0)) {
                                var instructions = [instruction]

                                for (var k = 1; k < 25; k++) {
                                    var potentialInstruction = oldInstructions.get(i + k)

                                    if (potentialInstruction.getOpcode() !== -1)
                                        instructions.push(potentialInstruction)
                                }

                                if (checkObfuscatedFieldInsn(instructions[1], Opcodes.GETFIELD, "net/minecraft/entity/projectile/FishingBobberEntity", "angler", "field_146042_b", "Lnet/minecraft/entity/player/PlayerEntity;")
                                        && checkObfuscatedMethodInsn(instructions[2], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getPosX", "func_226277_ct_", "()D")
                                        && checkVarInsn(instructions[3], Opcodes.ALOAD, 0) && checkObfuscatedMethodInsn(instructions[4], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getPosX", "func_226277_ct_", "()D")
                                        && checkInsn(instructions[5], Opcodes.DSUB) && checkVarInsn(instructions[6], Opcodes.DSTORE, 10)

                                        && checkVarInsn(instructions[7], Opcodes.ALOAD, 0) && checkObfuscatedFieldInsn(instructions[8], Opcodes.GETFIELD, "net/minecraft/entity/projectile/FishingBobberEntity", "angler", "field_146042_b", "Lnet/minecraft/entity/player/PlayerEntity;")
                                        && checkObfuscatedMethodInsn(instructions[9], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getPosY", "func_226278_cu_", "()D")
                                        && checkVarInsn(instructions[10], Opcodes.ALOAD, 0) && checkObfuscatedMethodInsn(instructions[11], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getPosY", "func_226278_cu_", "()D")
                                        && checkInsn(instructions[12], Opcodes.DSUB) && checkVarInsn(instructions[13], Opcodes.DSTORE, 12)

                                        && checkVarInsn(instructions[14], Opcodes.ALOAD, 0) && checkObfuscatedFieldInsn(instructions[15], Opcodes.GETFIELD, "net/minecraft/entity/projectile/FishingBobberEntity", "angler", "field_146042_b", "Lnet/minecraft/entity/player/PlayerEntity;")
                                        && checkObfuscatedMethodInsn(instructions[16], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getPosZ", "func_226281_cx_", "()D")
                                        && checkVarInsn(instructions[17], Opcodes.ALOAD, 0) && checkObfuscatedMethodInsn(instructions[18], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getPosZ", "func_226281_cx_", "()D")
                                        && checkInsn(instructions[19], Opcodes.DSUB) && checkVarInsn(instructions[20], Opcodes.DSTORE, 14)) {
                                    var getVectorList = new InsnList()
                                    var addXList = new InsnList()
                                    var addYList = new InsnList()
                                    var addZList = new InsnList()


                                    getVectorList.add(new VarInsnNode(Opcodes.ALOAD, 0))
                                    getVectorList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getAngler",
                                        "(Lnet/minecraft/entity/projectile/FishingBobberEntity;)Lnet/minecraft/entity/player/PlayerEntity;",
                                        false
                                    ))
                                    getVectorList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                        "getOffsets",
                                        "(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/math/Vec3d;",
                                        false
                                    ))
                                    getVectorList.add(new InsnNode(Opcodes.DUP))
                                    getVectorList.add(new InsnNode(Opcodes.DUP))

                                    addXList.add(new InsnNode(Opcodes.DUP2_X1))
                                    addXList.add(new InsnNode(Opcodes.POP2))
                                    addXList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorX",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addXList.add(new InsnNode(Opcodes.DADD))

                                    addYList.add(new InsnNode(Opcodes.DUP2_X1))
                                    addYList.add(new InsnNode(Opcodes.POP2))
                                    addYList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorY",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addYList.add(new InsnNode(Opcodes.DADD))

                                    addZList.add(new InsnNode(Opcodes.DUP2_X1))
                                    addZList.add(new InsnNode(Opcodes.POP2))
                                    addZList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorZ",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addZList.add(new InsnNode(Opcodes.DADD))


                                    // ...
                                    oldInstructions.insertBefore(instructions[0], getVectorList)
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/projectile/FishingBobberEntity.angler : Lnet/minecraft/entity/player/PlayerEntity;
                                    // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getPosX ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/projectile/FishingBobberEntity.getPosX ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[5], addXList)
                                    // DSTORE 10
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/projectile/FishingBobberEntity.angler : Lnet/minecraft/entity/player/PlayerEntity;
                                    // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getPosY ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/projectile/FishingBobberEntity.getPosY ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[12], addYList)
                                    // DSTORE 10
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/projectile/FishingBobberEntity.angler : Lnet/minecraft/entity/player/PlayerEntity;
                                    // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getPosZ ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/projectile/FishingBobberEntity.getPosZ ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[19], addZList)
                                    // DSTORE 14
                                    // ...

                                    logTransformSuccess("function handleHookRetraction", "net.minecraft.entity.projectile.FishingBobberEntity")
                                    success = true

                                    break
                                }
                            }
                        }

                        if (!success)
                            logTransformError("function handleHookRetraction", "net.minecraft.entity.projectile.FishingBobberEntity", "Unable to find injection points")

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

                        for (var i = 0; i < oldInstructions.size() - 18; i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkVarInsn(instruction, Opcodes.ALOAD, 0)) {
                                var instructions = [instruction]

                                for (var k = 1; k < 18; k++)
                                    instructions.push(oldInstructions.get(i + k))

                                if (checkObfuscatedFieldInsn(instructions[1], Opcodes.GETFIELD, "net/minecraft/entity/projectile/FishingBobberEntity", "angler", "field_146042_b", "Lnet/minecraft/entity/player/PlayerEntity;")
                                        && checkObfuscatedMethodInsn(instructions[2], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getPosX", "func_226277_ct_", "()D")
                                        && checkVarInsn(instructions[3], Opcodes.ALOAD, 0) && checkObfuscatedMethodInsn(instructions[4], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getPosX", "func_226277_ct_", "()D")
                                        && checkInsn(instructions[5], Opcodes.DSUB)

                                        && checkVarInsn(instructions[6], Opcodes.ALOAD, 0) && checkObfuscatedFieldInsn(instructions[7], Opcodes.GETFIELD, "net/minecraft/entity/projectile/FishingBobberEntity", "angler", "field_146042_b", "Lnet/minecraft/entity/player/PlayerEntity;")
                                        && checkObfuscatedMethodInsn(instructions[8], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getPosY", "func_226278_cu_", "()D")
                                        && checkVarInsn(instructions[9], Opcodes.ALOAD, 0) && checkObfuscatedMethodInsn(instructions[10], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getPosY", "func_226278_cu_", "()D")
                                        && checkInsn(instructions[11], Opcodes.DSUB)

                                        && checkVarInsn(instructions[12], Opcodes.ALOAD, 0) && checkObfuscatedFieldInsn(instructions[13], Opcodes.GETFIELD, "net/minecraft/entity/projectile/FishingBobberEntity", "angler", "field_146042_b", "Lnet/minecraft/entity/player/PlayerEntity;")
                                        && checkObfuscatedMethodInsn(instructions[14], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/PlayerEntity", "getPosZ", "func_226281_cx_", "()D")
                                        && checkVarInsn(instructions[15], Opcodes.ALOAD, 0) && checkObfuscatedMethodInsn(instructions[16], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/projectile/FishingBobberEntity", "getPosZ", "func_226281_cx_", "()D")
                                        && checkInsn(instructions[17], Opcodes.DSUB)) {
                                    var getVectorList = new InsnList()
                                    var vectorIndex = bringInHookedEntity.maxLocals
                                    var addXList = new InsnList()
                                    var addYList = new InsnList()
                                    var addZList = new InsnList()

                                    getVectorList.add(new VarInsnNode(Opcodes.ALOAD, 0))
                                    getVectorList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getAngler",
                                        "(Lnet/minecraft/entity/projectile/FishingBobberEntity;)Lnet/minecraft/entity/player/PlayerEntity;",
                                        false
                                    ))
                                    getVectorList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                        "getOffsets",
                                        "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                        false
                                    ))
                                    getVectorList.add(new VarInsnNode(Opcodes.ASTORE, vectorIndex))


                                    addXList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex))
                                    addXList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorX",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addXList.add(new InsnNode(Opcodes.DADD))

                                    addYList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex))
                                    addYList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorY",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addYList.add(new InsnNode(Opcodes.DADD))

                                    addZList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex))
                                    addZList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorZ",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addZList.add(new InsnNode(Opcodes.DADD))


                                    // ...
                                    oldInstructions.insertBefore(instructions[5], getVectorList)
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/projectile/FishingBobberEntity.angler : Lnet/minecraft/entity/player/PlayerEntity;
                                    // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getPosX ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/projectile/FishingBobberEntity.getPosX ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[5], addXList)
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/projectile/FishingBobberEntity.angler : Lnet/minecraft/entity/player/PlayerEntity;
                                    // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getPosY ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/projectile/FishingBobberEntity.getPosY ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[11], addYList)
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/projectile/FishingBobberEntity.angler : Lnet/minecraft/entity/player/PlayerEntity;
                                    // INVOKEVIRTUAL net/minecraft/entity/player/PlayerEntity.getPosZ ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/projectile/FishingBobberEntity.getPosZ ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[17], addZList)
                                    // ...

                                    logTransformSuccess("function bringInHookedEntity", "net.minecraft.entity.projectile.FishingBobberEntity")
                                    success = true

                                    break
                                }
                            }
                        }

                        if (!success)
                            logTransformError("function bringInHookedEntity", "net.minecraft.entity.projectile.FishingBobberEntity", "Unable to find injection points")

                    } catch (exception) {
                        logTransformError("function bringInHookedEntity", "net.minecraft.entity.projectile.FishingBobberEntity", exception.message)
                    }

                } else
                    logTransformError("function bringInHookedEntity", "net.minecraft.entity.projectile.FishingBobberEntity", "Unable to find function to transform")

                return classNode
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
                    var oldInstructions = methodNode.instructions
                    var success = false

                    // Puts in offset to fishing line position.
                    for (var i = 0; i < oldInstructions.size() - 38; i++) {
                        var instruction = oldInstructions.get(i)

                        if (checkVarInsn(instruction, Opcodes.ALOAD, 0)) {
                            var instructions = [instruction]

                            for (var k = 1; k < 38; k++) {
                                var potentialInstruction = oldInstructions.get(i + k)

                                if (potentialInstruction.getOpcode() !== -1)
                                    instructions.push(potentialInstruction)
                            }

                            if (checkObfuscatedFieldInsn(instructions[1], Opcodes.GETFIELD, "net/minecraft/server/management/PlayerInteractionManager", "player", "field_73090_b", "Lnet/minecraft/entity/player/ServerPlayerEntity;")
                                    && checkObfuscatedMethodInsn(instructions[2], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/ServerPlayerEntity", "getPosX", "func_226277_ct_", "()D")
                                    && checkVarInsn(instructions[3], Opcodes.ALOAD, 1) && checkObfuscatedMethodInsn(instructions[4], Opcodes.INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", "getX", "func_177958_n", "()I")
                                    && checkInsn(instructions[5], Opcodes.I2D) && checkLdcInsn(instructions[6], 0.5) && checkInsn(instructions[7], Opcodes.DADD)
                                    && checkInsn(instructions[8], Opcodes.DSUB) && checkVarInsn(instructions[9], Opcodes.DSTORE, 5)

                                    && checkVarInsn(instructions[10], Opcodes.ALOAD, 0) && checkObfuscatedFieldInsn(instructions[11], Opcodes.GETFIELD, "net/minecraft/server/management/PlayerInteractionManager", "player", "field_73090_b", "Lnet/minecraft/entity/player/ServerPlayerEntity;")
                                    && checkObfuscatedMethodInsn(instructions[12], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/ServerPlayerEntity", "getPosY", "func_226278_cu_", "()D")
                                    && checkVarInsn(instructions[13], Opcodes.ALOAD, 1) && checkObfuscatedMethodInsn(instructions[14], Opcodes.INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", "getY", "func_177956_o", "()I")
                                    && checkInsn(instructions[15], Opcodes.I2D) && checkLdcInsn(instructions[16], 0.5) && checkInsn(instructions[17], Opcodes.DADD)
                                    && checkInsn(instructions[18], Opcodes.DSUB) && checkLdcInsn(instructions[19], 1.5) && checkInsn(instructions[20], Opcodes.DADD)
                                    && checkVarInsn(instructions[21], Opcodes.DSTORE, 7)

                                    && checkVarInsn(instructions[22], Opcodes.ALOAD, 0) && checkObfuscatedFieldInsn(instructions[23], Opcodes.GETFIELD, "net/minecraft/server/management/PlayerInteractionManager", "player", "field_73090_b", "Lnet/minecraft/entity/player/ServerPlayerEntity;")
                                    && checkObfuscatedMethodInsn(instructions[24], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/player/ServerPlayerEntity", "getPosZ", "func_226281_cx_", "()D")
                                    && checkVarInsn(instructions[25], Opcodes.ALOAD, 1) && checkObfuscatedMethodInsn(instructions[26], Opcodes.INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", "getZ", "func_177952_p", "()I")
                                    && checkInsn(instructions[27], Opcodes.I2D) && checkLdcInsn(instructions[28], 0.5) && checkInsn(instructions[29], Opcodes.DADD)
                                    && checkInsn(instructions[30], Opcodes.DSUB) && checkVarInsn(instructions[31], Opcodes.DSTORE, 9)) {
                                var getVectorList = new InsnList()
                                var addXList = new InsnList()
                                var addYList = new InsnList()
                                var addZList = new InsnList()


                                getVectorList.add(new VarInsnNode(Opcodes.ALOAD, 0))
                                getVectorList.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getPlayerFromManager",
                                    "(Lnet/minecraft/server/management/PlayerInteractionManager;)Lnet/minecraft/entity/player/ServerPlayerEntity;",
                                    false
                                ))
                                getVectorList.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "getOffsets",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))
                                getVectorList.add(new InsnNode(Opcodes.DUP))
                                getVectorList.add(new InsnNode(Opcodes.DUP))

                                addXList.add(new InsnNode(Opcodes.DUP2_X1))
                                addXList.add(new InsnNode(Opcodes.POP2))
                                addXList.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorX",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                addXList.add(new InsnNode(Opcodes.DADD))

                                addYList.add(new InsnNode(Opcodes.DUP2_X1))
                                addYList.add(new InsnNode(Opcodes.POP2))
                                addYList.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorY",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                addYList.add(new InsnNode(Opcodes.DADD))

                                addZList.add(new InsnNode(Opcodes.DUP2_X1))
                                addZList.add(new InsnNode(Opcodes.POP2))
                                addZList.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getVectorZ",
                                    "(Lnet/minecraft/util/math/Vec3d;)D",
                                    false
                                ))
                                addZList.add(new InsnNode(Opcodes.DADD))


                                // ...
                                oldInstructions.insertBefore(instructions[0], getVectorList)
                                // ALOAD 0
                                // GETFIELD net/minecraft/server/management/PlayerInteractionManager.player : Lnet/minecraft/entity/player/ServerPlayerEntity;
                                //INVOKEVIRTUAL net/minecraft/entity/player/ServerPlayerEntity.getPosX ()D
                                // ALOAD 1
                                // INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getX ()I
                                // I2D
                                // LDC 0.5
                                // DADD
                                // DSUB
                                oldInstructions.insert(instructions[8], addXList)
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
                                oldInstructions.insert(instructions[20], addYList)
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
                                oldInstructions.insert(instructions[30], addZList)
                                // DSTORE 9
                                // ...

                                logTransformSuccess("function func_225416_a", "net.minecraft.server.management.PlayerInteractionManager")
                                success = true

                                break
                            }
                        }
                    }

                    if (!success)
                        logTransformError("function func_225416_a", "net.minecraft.server.management.PlayerInteractionManager", "Unable to find injection points")

                } catch (exception) {
                    logTransformError("function func_225416_a", "net.minecraft.server.management.PlayerInteractionManager", exception.message)
                }

                return methodNode
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
                    try {
                        var oldInstructions = updateLeashedState.instructions
                        var successes = 0

                        // Modifies home position set for leashes.
                        for (var i = 0; i < oldInstructions.size() - 4; i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkTypeInsn(instruction, Opcodes.NEW, "net/minecraft/util/math/BlockPos")) {
                                var instructions = [instruction]

                                for (var k = 1; k < 4; k++)
                                    instructions.push(oldInstructions.get(i + k))

                                if (checkInsn(instructions[1], Opcodes.DUP) && checkVarInsn(instructions[2], Opcodes.ALOAD, 1)
                                        && checkMethodInsn(instructions[3], Opcodes.INVOKESPECIAL, "net/minecraft/util/math/BlockPos", "<init>", "(Lnet/minecraft/entity/Entity;)V")) {
                                    var newInstructions = new InsnList()

                                    newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1))
                                    newInstructions.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                        "modifiedBlockPos",
                                        "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/BlockPos;"
                                    ))

                                    oldInstructions.insertBefore(instructions[0], newInstructions)

                                    for (var k = 0; k < instructions.length; k++)
                                        oldInstructions.remove(instructions[k])


                                    successes |= 1
                                    break
                                }
                            }
                        }

                        if (successes & 1 === 0)
                            logTransformError("function updateLeashedState", "net.minecraft.entity.CreatureEntity", "Unable to find primary injection points")


                        // Modifies distance calculation for leashes.
                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getDistance", "func_70032_d",
                                    "(Lnet/minecraft/entity/Entity;)F")) {
                                oldInstructions.insert(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "modifiedGetDistance",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;)F",
                                    false
                                ))
                                oldInstructions.remove(instruction)

                                successes |= 2
                                break
                            }
                        }

                        if (successes & 2 === 0)
                            logTransformError("function updateLeashedState", "net.minecraft.entity.CreatureEntity", "Unable to find secondary injection points")


                        // Modifies pull position for leashes.
                        for (var i = 0; i < oldInstructions.size() - 31; i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkVarInsn(instruction, Opcodes.ALOAD, 1)) {
                                var instructions = [instruction]

                                for (var k = 1; k < 31; k++) {
                                    var potentialInstruction = oldInstructions.get(i + k)

                                    if (potentialInstruction.getOpcode() !== -1)
                                        instructions.push(potentialInstruction)
                                }

                                if (checkObfuscatedMethodInsn(instructions[1], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosX", "func_226277_ct_", "()D")
                                        && checkVarInsn(instructions[2], Opcodes.ALOAD, 0) && checkObfuscatedMethodInsn(instructions[3], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getPosX", "func_226277_ct_", "()D")
                                        && checkInsn(instructions[4], Opcodes.DSUB) && checkVarInsn(instructions[5], Opcodes.FLOAD, 2) && checkInsn(instructions[6], Opcodes.F2D)
                                        && checkInsn(instructions[7], Opcodes.DDIV) && checkVarInsn(instructions[8], Opcodes.DSTORE, 3)

                                        && checkVarInsn(instructions[9], Opcodes.ALOAD, 1) && checkObfuscatedMethodInsn(instructions[10], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosY", "func_226278_cu_", "()D")
                                        && checkVarInsn(instructions[11], Opcodes.ALOAD, 0) && checkObfuscatedMethodInsn(instructions[12], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getPosY", "func_226278_cu_", "()D")
                                        && checkInsn(instructions[13], Opcodes.DSUB) && checkVarInsn(instructions[14], Opcodes.FLOAD, 2) && checkInsn(instructions[15], Opcodes.F2D) && checkInsn(instructions[16], Opcodes.DDIV)
                                        && checkVarInsn(instructions[17], Opcodes.DSTORE, 5)

                                        && checkVarInsn(instructions[18], Opcodes.ALOAD, 1) && checkObfuscatedMethodInsn(instructions[19], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosZ", "func_226281_cx_", "()D")
                                        && checkVarInsn(instructions[20], Opcodes.ALOAD, 0) && checkObfuscatedMethodInsn(instructions[21], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getPosZ", "func_226281_cx_", "()D")
                                        && checkInsn(instructions[22], Opcodes.DSUB) && checkVarInsn(instructions[23], Opcodes.FLOAD, 2) && checkInsn(instructions[24], Opcodes.F2D) && checkInsn(instructions[25], Opcodes.DDIV) &&
                                        checkVarInsn(instructions[26], Opcodes.DSTORE, 7)) {
                                    var getVectorList = new InsnList()
                                    var addXList = new InsnList()
                                    var addYList = new InsnList()
                                    var addZList = new InsnList()


                                    getVectorList.add(new VarInsnNode(Opcodes.ALOAD, 1))
                                    getVectorList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                        "getOffsets",
                                        "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                        false
                                    ))
                                    getVectorList.add(new InsnNode(Opcodes.DUP))
                                    getVectorList.add(new InsnNode(Opcodes.DUP))

                                    addXList.add(new InsnNode(Opcodes.DUP2_X1))
                                    addXList.add(new InsnNode(Opcodes.POP2))
                                    addXList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorX",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addXList.add(new InsnNode(Opcodes.DADD))

                                    addYList.add(new InsnNode(Opcodes.DUP2_X1))
                                    addYList.add(new InsnNode(Opcodes.POP2))
                                    addYList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorY",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addYList.add(new InsnNode(Opcodes.DADD))

                                    addZList.add(new InsnNode(Opcodes.DUP2_X1))
                                    addZList.add(new InsnNode(Opcodes.POP2))
                                    addZList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorZ",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addZList.add(new InsnNode(Opcodes.DADD))


                                    // ...
                                    oldInstructions.insertBefore(instructions[0], getVectorList)
                                    // ALOAD 1
                                    // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosX ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/CreatureEntity.getPosX ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[4], addXList)
                                    // FLOAD 2
                                    // F2D
                                    // DDIV
                                    // DSTORE 3
                                    // ALOAD 1
                                    // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosY ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/CreatureEntity.getPosY ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[13], addYList)
                                    // FLOAD 2
                                    // F2D
                                    // DDIV
                                    // DSTORE 5
                                    // ALOAD 1
                                    // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosZ ()D
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/CreatureEntity.getPosZ ()D
                                    // DSUB
                                    oldInstructions.insert(instructions[22], addZList)
                                    // FLOAD 2
                                    // F2D
                                    // DDIV
                                    // DSTORE 7
                                    // ...

                                    successes |= 4
                                    break
                                }
                            }
                        }

                        if (successes & 4 === 0)
                            logTransformError("function updateLeashedState", "net.minecraft.entity.CreatureEntity", "Unable to find tertiary injection points")


                        // Modifies AI move behavior when leashed.
                        for (var i = 0; i < oldInstructions.size() - 27; i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkTypeInsn(instruction, Opcodes.NEW, "net/minecraft/util/math/Vec3d")) {
                                var instructions = [instruction]

                                for (var k = 1; k < 27; k++)
                                    instructions.push(oldInstructions.get(i + k))

                                if (checkInsn(instructions[1], Opcodes.DUP) &&
                                        checkVarInsn(instructions[2], Opcodes.ALOAD, 1) && checkObfuscatedMethodInsn(instructions[3], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosX", "func_226277_ct_", "()D") && checkVarInsn(instructions[4], Opcodes.ALOAD, 0) && checkObfuscatedMethodInsn(instructions[5], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getPosX", "func_226277_ct_", "()D") && checkInsn(instructions[6], Opcodes.DSUB) &&
                                        checkVarInsn(instructions[7], Opcodes.ALOAD, 1) && checkObfuscatedMethodInsn(instructions[8], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosY", "func_226278_cu_", "()D") && checkVarInsn(instructions[9], Opcodes.ALOAD, 0) && checkObfuscatedMethodInsn(instructions[10], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getPosY", "func_226278_cu_", "()D") && checkInsn(instructions[11], Opcodes.DSUB) &&
                                        checkVarInsn(instructions[12], Opcodes.ALOAD, 1) && checkObfuscatedMethodInsn(instructions[13], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosZ", "func_226281_cx_", "()D") && checkVarInsn(instructions[14], Opcodes.ALOAD, 0) && checkObfuscatedMethodInsn(instructions[15], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/CreatureEntity", "getPosZ", "func_226281_cx_", "()D") && checkInsn(instructions[16], Opcodes.DSUB) &&
                                        checkMethodInsn(instructions[17], Opcodes.INVOKESPECIAL, "net/minecraft/util/math/Vec3d", "<init>", "(DDD)V") && checkObfuscatedMethodInsn(instructions[18], Opcodes.INVOKEVIRTUAL, "net/minecraft/util/math/Vec3d", "normalize", "func_72432_b", "()Lnet/minecraft/util/math/Vec3d;") && checkVarInsn(instructions[19], Opcodes.FLOAD, 2) && checkInsn(instructions[20], Opcodes.FCONST_2) && checkInsn(instructions[21], Opcodes.FSUB) && checkInsn(instructions[22], Opcodes.FCONST_0) && checkMethodInsn(instructions[23], Opcodes.INVOKESTATIC, "java/lang/Math", "max", "(FF)F") && checkInsn(instructions[24], Opcodes.F2D) && checkObfuscatedMethodInsn(instructions[25], Opcodes.INVOKEVIRTUAL, "net/minecraft/util/math/Vec3d", "scale", "func_186678_a", "(D)Lnet/minecraft/util/math/Vec3d;") && checkVarInsn(instructions[26], Opcodes.ASTORE, 4)) {
                                    var getVectorList = new InsnList()
                                    var addXList = new InsnList()
                                    var addYList = new InsnList()
                                    var addZList = new InsnList()
                                    var vectorIndex = updateLeashedState.maxLocals


                                    getVectorList.add(new VarInsnNode(Opcodes.ALOAD, 1))
                                    getVectorList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                        "getOffsets",
                                        "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                        false
                                    ))
                                    getVectorList.add(new VarInsnNode(Opcodes.ASTORE, vectorIndex))

                                    addXList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex))
                                    addXList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorX",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addXList.add(new InsnNode(Opcodes.DADD))

                                    addYList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex))
                                    addYList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorY",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addYList.add(new InsnNode(Opcodes.DADD))

                                    addZList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex))
                                    addZList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorZ",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addZList.add(new InsnNode(Opcodes.DADD))


                                    // ...
                                    oldInstructions.insertBefore(instructions[0], getVectorList)
                                    // NEW net/minecraft/util/math/Vec3d
                                    // DUP
                                    // ALOAD 1
                                    // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosX ()D
                                    oldInstructions.insert(instructions[3], addXList)
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/CreatureEntity.getPosX ()D
                                    // DSUB
                                    // ALOAD 1
                                    // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosY ()D
                                    oldInstructions.insert(instructions[8], addYList)
                                    // ALOAD 0
                                    // INVOKEVIRTUAL net/minecraft/entity/CreatureEntity.getPosY ()D
                                    // DSUB
                                    // ALOAD 1
                                    // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosZ ()D
                                    oldInstructions.insert(instructions[13], addZList)
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

                                    successes |= 8
                                    break
                                }
                            }
                        }

                        if (successes & 8 === 0) {
                            logTransformError("function updateLeashedState", "net.minecraft.entity.CreatureEntity", "Unable to find the fourth set of injection points")

                        } else if (successes ^ 15 === 0)
                            logTransformSuccess("function updateLeashedState", "net.minecraft.entity.CreatureEntity")

                    } catch (exception) {
                        logTransformError("function updateLeashedState", "net.minecraft.entity.CreatureEntity", exception.message)
                    }

                } else
                    logTransformError("function updateLeashedState", "net.minecraft.entity.CreatureEntity", "Unable to find function to transform")

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
                        var successes = 0
                        var vectorIndex = renderLeash.maxLocals

                        // Modifies the x-component of the leash render position.
                        for (var i = 0; i < oldInstructions.size() - 7; i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkVarInsn(instruction, Opcodes.FLOAD, 2)) {
                                var instructions = [instruction]

                                for (var k = 1; k < 7; k++)
                                    instructions.push(oldInstructions.get(i + k))

                                if (checkInsn(instructions[1], Opcodes.F2D) && checkVarInsn(instructions[2], Opcodes.ALOAD, 5) && checkObfuscatedFieldInsn(instructions[3], Opcodes.GETFIELD, "net/minecraft/entity/Entity", "prevPosX", "field_70169_q", "D")
                                        && checkVarInsn(instructions[4], Opcodes.ALOAD, 5) && checkObfuscatedMethodInsn(instructions[5], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosX", "func_226277_ct_", "()D")
                                        && checkObfuscatedMethodInsn(instructions[6], Opcodes.INVOKESTATIC, "net/minecraft/util/math/MathHelper", "lerp", "func_219803_d", "(DDD)D")) {
                                    var getVectorList = new InsnList()
                                    var addXList = new InsnList()


                                    getVectorList.add(new VarInsnNode(Opcodes.ALOAD, 5))
                                    getVectorList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                        "getOffsets",
                                        "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                        false
                                    ))
                                    getVectorList.add(new VarInsnNode(Opcodes.ASTORE, vectorIndex))

                                    addXList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex))
                                    addXList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorX",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addXList.add(new InsnNode(Opcodes.DADD))


                                    // ...
                                    oldInstructions.insertBefore(instructions[0], getVectorList)
                                    // FLOAD 2
                                    // F2D
                                    // ALOAD 5
                                    // GETFIELD net/minecraft/entity/Entity.prevPosX : D
                                    // ALOAD 5
                                    // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosX ()D
                                    // INVOKESTATIC net/minecraft/util/math/MathHelper.lerp (DDD)D
                                    oldInstructions.insert(instructions[6], addXList)
                                    // ...

                                    successes |= 1
                                    break
                                }
                            }
                        }

                        if (successes & 1 === 0) {
                            logTransformError("function renderLeash", "net.minecraft.client.renderer.entity.MobRenderer", "Unable to find primary injection points")

                        } else {
                            // Modifies the y-component of the leash render position.
                            for (var i = 0; i < oldInstructions.size() - 15; i++) {
                                var instruction = oldInstructions.get(i)

                                if (checkVarInsn(instruction, Opcodes.FLOAD, 2)) {
                                    var instructions = [instruction]

                                    for (var k = 1; k < 15; k++)
                                        instructions.push(oldInstructions.get(i + k))

                                    if (checkInsn(instructions[1], Opcodes.F2D) && checkVarInsn(instructions[2], Opcodes.ALOAD, 5) && checkObfuscatedFieldInsn(instructions[3], Opcodes.GETFIELD, "net/minecraft/entity/Entity", "prevPosY", "field_70167_r", "D")
                                            && checkVarInsn(instructions[4], Opcodes.ALOAD, 5) && checkObfuscatedMethodInsn(instructions[5], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEyeHeight", "func_70047_e", "()F")
                                            && checkInsn(instructions[6], Opcodes.F2D) && checkLdcInsn(instructions[7], 0.7) && checkInsn(instructions[8], Opcodes.DMUL)
                                            && checkInsn(instructions[9], Opcodes.DADD)

                                            && checkVarInsn(instructions[10], Opcodes.ALOAD, 5) && checkObfuscatedMethodInsn(instructions[11], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosY", "func_226278_cu_", "()D")
                                            && checkVarInsn(instructions[12], Opcodes.ALOAD, 5) && checkObfuscatedMethodInsn(instructions[13], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getEyeHeight", "func_70047_e", "()F")
                                            && checkInsn(instructions[14], Opcodes.F2D)) {
                                        var addYList = new InsnList()

                                        addYList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex))
                                        addYList.add(new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                            "getVectorY",
                                            "(Lnet/minecraft/util/math/Vec3d;)D",
                                            false
                                        ))
                                        addYList.add(new InsnNode(Opcodes.DADD))

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
                                        oldInstructions.insert(instructions[14], addYList)
                                        // ...

                                        successes |= 2
                                        break
                                    }
                                }
                            }

                            if (successes & 2 === 0)
                                logTransformError("function renderLeash", "net.minecraft.client.renderer.entity.MobRenderer", "Unable to find secondary injection points")


                            // Modifies the z-component of the leash render position.
                            for (var i = 0; i < oldInstructions.size() - 7; i++) {
                                var instruction = oldInstructions.get(i)

                                if (checkVarInsn(instruction, Opcodes.FLOAD, 2)) {
                                    var instructions = [instruction]

                                    for (var k = 1; k < 7; k++)
                                        instructions.push(oldInstructions.get(i + k))

                                    if (checkInsn(instructions[1], Opcodes.F2D) && checkVarInsn(instructions[2], Opcodes.ALOAD, 5) && checkObfuscatedFieldInsn(instructions[3], Opcodes.GETFIELD, "net/minecraft/entity/Entity", "prevPosZ", "field_70166_s", "D")
                                            && checkVarInsn(instructions[4], Opcodes.ALOAD, 5) && checkObfuscatedMethodInsn(instructions[5], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosZ", "func_226281_cx_", "()D")
                                            && checkObfuscatedMethodInsn(instructions[6], Opcodes.INVOKESTATIC, "net/minecraft/util/math/MathHelper", "lerp", "func_219803_d", "(DDD)D")) {
                                        var addZList = new InsnList()

                                        addZList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex))
                                        addZList.add(new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                            "getVectorZ",
                                            "(Lnet/minecraft/util/math/Vec3d;)D",
                                            false
                                        ))
                                        addZList.add(new InsnNode(Opcodes.DADD))

                                        // ...
                                        // FLOAD 2
                                        // F2D
                                        // ALOAD 5
                                        // GETFIELD net/minecraft/entity/Entity.prevPosZ : D
                                        // ALOAD 5
                                        // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosZ ()D
                                        // INVOKESTATIC net/minecraft/util/math/MathHelper.lerp (DDD)D
                                        oldInstructions.insert(instructions[6], addZList)
                                        // ...

                                        successes |= 4
                                        break
                                    }
                                }
                            }

                            if (successes & 4 === 0) {
                                logTransformError("function renderLeash", "net.minecraft.client.renderer.entity.MobRenderer", "Unable to find tertiary injection points")

                            } else if (successes ^ 7 === 0)
                                logTransformSuccess("function renderLeash", "net.minecraft.client.renderer.entity.MobRenderer")
                        }

                    } catch (exception) {
                        logTransformError("function renderLeash", "net.minecraft.client.renderer.entity.MobRenderer", exception.message)
                    }

                } else
                    logTransformError("function renderLeash", "net.minecraft.client.renderer.entity.MobRenderer", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Makes entities look at the offset position of players.
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

                        // Puts in offset to fishing line position.
                        for (var i = 0; i < oldInstructions.size() - 9; i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkVarInsn(instruction, Opcodes.ALOAD, 0)) {
                                var instructions = [instruction]

                                for (var k = 1; k < 9; k++)
                                    instructions.push(oldInstructions.get(i + k))

                                if (checkObfuscatedFieldInsn(instructions[1], Opcodes.GETFIELD, "net/minecraft/entity/ai/goal/LookAtGoal", "closestEntity", "field_75334_a", "Lnet/minecraft/entity/Entity;")
                                        && checkObfuscatedMethodInsn(instructions[2], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosX", "func_226277_ct_", "()D")

                                        && checkVarInsn(instructions[3], Opcodes.ALOAD, 0) && checkObfuscatedFieldInsn(instructions[4], Opcodes.GETFIELD, "net/minecraft/entity/ai/goal/LookAtGoal", "closestEntity", "field_75334_a", "Lnet/minecraft/entity/Entity;")
                                        && checkObfuscatedMethodInsn(instructions[5], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosYEye", "func_226280_cw_", "()D")

                                        && checkVarInsn(instructions[6], Opcodes.ALOAD, 0) && checkObfuscatedFieldInsn(instructions[7], Opcodes.GETFIELD, "net/minecraft/entity/ai/goal/LookAtGoal", "closestEntity", "field_75334_a", "Lnet/minecraft/entity/Entity;")
                                        && checkObfuscatedMethodInsn(instructions[8], Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosZ", "func_226281_cx_", "()D")) {
                                    var getVectorList = new InsnList()
                                    var addXList = new InsnList()
                                    var addYList = new InsnList()
                                    var addZList = new InsnList()
                                    var vectorIndex = tick.maxLocals


                                    getVectorList.add(new VarInsnNode(Opcodes.ALOAD, 0))
                                    getVectorList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getClosestEntity",
                                        "(Lnet/minecraft/entity/ai/goal/LookAtGoal;)Lnet/minecraft/entity/Entity;"
                                    ))
                                    getVectorList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                        "getOffsets",
                                        "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                        false
                                    ))
                                    getVectorList.add(new VarInsnNode(Opcodes.ASTORE, vectorIndex))

                                    addXList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex))
                                    addXList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorX",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addXList.add(new InsnNode(Opcodes.DADD))

                                    addYList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex))
                                    addYList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorY",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addYList.add(new InsnNode(Opcodes.DADD))

                                    addZList.add(new VarInsnNode(Opcodes.ALOAD, vectorIndex))
                                    addZList.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                        "getVectorZ",
                                        "(Lnet/minecraft/util/math/Vec3d;)D",
                                        false
                                    ))
                                    addZList.add(new InsnNode(Opcodes.DADD))


                                    oldInstructions.insertBefore(instructions[0], getVectorList)
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/ai/goal/LookAtGoal.closestEntity : Lnet/minecraft/entity/Entity;
                                    // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosX ()D
                                    oldInstructions.insert(instructions[2], addXList)
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/ai/goal/LookAtGoal.closestEntity : Lnet/minecraft/entity/Entity;
                                    // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosYEye ()D
                                    oldInstructions.insert(instructions[5], addYList)
                                    // ALOAD 0
                                    // GETFIELD net/minecraft/entity/ai/goal/LookAtGoal.closestEntity : Lnet/minecraft/entity/Entity;
                                    // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosZ ()D
                                    oldInstructions.insert(instructions[8], addZList)
                                    // ...

                                    success = true
                                    break
                                }
                            }
                        }

                        if (success) {
                            logTransformSuccess("function tick", "net.minecraft.entity.ai.goal.LookAtGoal")

                        } else
                            logTransformError("function tick", "net.minecraft.entity.ai.goal.LookAtGoal", "Unable to find injection points")

                    } catch (exception) {
                        logTransformError("function tick", "net.minecraft.entity.ai.goal.LookAtGoal", exception.message)
                    }

                } else
                    logTransformError("function tick", "net.minecraft.entity.ai.goal.LookAtGoal", "Unable to find function to transform")

                return classNode
            }
        },

        /**
         * Allows players to interact with blocks relative to their offsets.
         */
        "ServerPlayNetHandler": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.network.play.ServerPlayNetHandler"
            },

            "transformer": function(classNode) {
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
                                oldInstructions.insert(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;DDD)D",
                                    false
                                ))

                                oldInstructions.remove(instruction)

                                success = true
                                break
                            }
                        }

                        if (success) {
                            logTransformSuccess("function processTryUseItemOnBlock", "net.minecraft.network.play.ServerPlayNetHandler")

                        } else
                            logTransformError("function processTryUseItemOnBlock", "net.minecraft.network.play.ServerPlayNetHandler", "Unable to find injection point")

                    } catch (exception) {
                        logTransformError("function processTryUseItemOnBlock", "net.minecraft.network.play.ServerPlayNetHandler", exception.message)
                    }

                } else
                    logTransformError("function processTryUseItemOnBlock", "net.minecraft.network.play.ServerPlayNetHandler", "Unable to find function to transform")

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
                                oldInstructions.insert(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;DDD)D",
                                    false
                                ))

                                oldInstructions.remove(instruction)

                                success = true
                                break
                            }
                        }

                        if (success) {
                            logTransformSuccess("function isUsableByPlayer", "net.minecraft.tileentity.AbstractFurnaceTileEntity")

                        } else
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
                                oldInstructions.insert(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;DDD)D",
                                    false
                                ))

                                oldInstructions.remove(instruction)

                                success = true
                                break
                            }
                        }

                        if (success) {
                            logTransformSuccess("function isUsableByPlayer", "net.minecraft.tileentity.BrewingStandTileEntity")

                        } else
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
                                oldInstructions.insert(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;DDD)D",
                                    false
                                ))

                                oldInstructions.remove(instruction)

                                success = true
                                break
                            }
                        }

                        if (success) {
                            logTransformSuccess("function isUsableByPlayer", "net.minecraft.tileentity.LockableLootTileEntity")

                        } else
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
                                oldInstructions.insert(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;DDD)D",
                                    false
                                ))

                                oldInstructions.remove(instruction)

                                success = true
                                break
                            }
                        }

                        if (success) {
                            logTransformSuccess("function isUsableByPlayer", "net.minecraft.tileentity.LecternTileEntity")

                        } else
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
                                oldInstructions.insert(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;DDD)D",
                                    false
                                ))

                                oldInstructions.remove(instruction)

                                success = true
                                break
                            }
                        }

                        if (success) {
                            logTransformSuccess("function canBeUsed", "net.minecraft.tileentity.EnderChestTileEntity")

                        } else
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
                                var getDistanceList = new InsnList()

                                getDistanceList.add(new InsnNode(Opcodes.SWAP))
                                getDistanceList.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/player/PlayerEntity;)D",
                                    false
                                ))

                                oldInstructions.insert(instruction, getDistanceList)
                                oldInstructions.remove(instruction)

                                success = true
                                break
                            }
                        }

                        if (success) {
                            logTransformSuccess("function isUsableByPlayer", "net.minecraft.entity.item.minecart.ContainerMinecartEntity")

                        } else
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
                                oldInstructions.insert(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "modifiedGetDistanceSq",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;DDD)D",
                                    false
                                ))

                                oldInstructions.remove(instruction)

                                success = true
                                break
                            }
                        }

                        if (success) {
                            logTransformSuccess("function lambda$isWithinUsableDistance$0", "net.minecraft.inventory.container.Container")

                        } else
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
                    try {
                        var oldInstructions = shouldRender.instructions
                        var successes = 0

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "isInRangeToRender3d", "func_145770_h",
                                    "(DDD)Z")) {
                                oldInstructions.insert(instruction, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/rendering/RenderingOffsetter",
                                    "modifiedIsInRangeToRender3d",
                                    "(Lnet/minecraft/entity/Entity;DDD)Z",
                                    false
                                ))

                                oldInstructions.remove(instruction)

                                successes |= 1
                                break
                            }
                        }

                        if (successes & 1 === 0)
                            logTransformError("function shouldRender", "net.minecraft.client.renderer.entity.EntityRenderer", "Unable to find primary injection point")

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/client/renderer/culling/ClippingHelperImpl",
                                    "isBoundingBoxInFrustum", "func_228957_a_", "(Lnet/minecraft/util/math/AxisAlignedBB;)Z")) {
                                var doubleCheckPlayer = new InsnList()
                                var skipToReturn = new LabelNode()
                                var noOffsetsReturn = new LabelNode()


                                // Returns if the entity should be rendered.
                                doubleCheckPlayer.add(new InsnNode(Opcodes.DUP))
                                doubleCheckPlayer.add(new JumpInsnNode(Opcodes.IFNE, skipToReturn))
                                // Returns if the entity is not a player.
                                doubleCheckPlayer.add(new VarInsnNode(Opcodes.ALOAD, 1)) // livingEntityIn.
                                doubleCheckPlayer.add(new TypeInsnNode(Opcodes.INSTANCEOF, "net/minecraft/entity/player/PlayerEntity"))
                                doubleCheckPlayer.add(new JumpInsnNode(Opcodes.IFEQ, skipToReturn))

                                // Gets the player's offset and stores it.
                                doubleCheckPlayer.add(new VarInsnNode(Opcodes.ALOAD, 1)) // livingEntityIn.
                                doubleCheckPlayer.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/entity/player/PlayerEntity"))
                                doubleCheckPlayer.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "getOffsets",
                                    "(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/math/Vec3d;",
                                    false
                                ))
                                doubleCheckPlayer.add(new InsnNode(Opcodes.DUP))
                                // Returns if the player has no offset.
                                doubleCheckPlayer.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "getZeroVector",
                                    "()Lnet/minecraft/util/math/Vec3d;"
                                ))
                                doubleCheckPlayer.add(new MethodInsnNode(
                                    Opcodes.INVOKEVIRTUAL,
                                    "net/minecraft/util/math/Vec3d",
                                    "equals",
                                    "(Ljava/lang/Object;)Z",
                                    false
                                ))
                                doubleCheckPlayer.add(new JumpInsnNode(Opcodes.IFNE, noOffsetsReturn))

                                // Discards result and runs second check, accounting for the player's offsets.
                                doubleCheckPlayer.add(new InsnNode(Opcodes.SWAP))
                                doubleCheckPlayer.add(new InsnNode(Opcodes.POP))
                                doubleCheckPlayer.add(new VarInsnNode(Opcodes.ALOAD, 2)) // camera.
                                doubleCheckPlayer.add(new InsnNode(Opcodes.SWAP))
                                // Offsets bounding box with the player's offsets.
                                doubleCheckPlayer.add(new VarInsnNode(Opcodes.ALOAD, 9)) // axisalignedbb.
                                doubleCheckPlayer.add(new InsnNode(Opcodes.SWAP))
                                doubleCheckPlayer.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "offsetAABB",
                                    "(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/AxisAlignedBB;"
                                ))
                                // Recalculates frustum collision.
                                doubleCheckPlayer.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                    "isBoundingBoxInFrustum",
                                    "(Lnet/minecraft/client/renderer/culling/ClippingHelperImpl;Lnet/minecraft/util/math/AxisAlignedBB;)Z"
                                ))
                                doubleCheckPlayer.add(new JumpInsnNode(Opcodes.GOTO, skipToReturn))

                                doubleCheckPlayer.add(noOffsetsReturn)
                                doubleCheckPlayer.add(new InsnNode(Opcodes.POP))

                                doubleCheckPlayer.add(skipToReturn)


                                //...
                                // INVOKEVIRTUAL net/minecraft/client/renderer/culling/ClippingHelperImpl.isBoundingBoxInFrustum (Lnet/minecraft/util/math/AxisAlignedBB;)Z
                                oldInstructions.insert(instruction, doubleCheckPlayer)
                                //...

                                successes |= 2
                                break
                            }
                        }

                        if (successes & 2 === 0) {
                            logTransformError("function shouldRender", "net.minecraft.client.renderer.entity.EntityRenderer", "Unable to find secondary injection point")

                        } else if (successes & 3 === 3)
                            logTransformSuccess("function shouldRender", "net.minecraft.client.renderer.entity.EntityRenderer")

                    } catch (exception) {
                        logTransformError("function shouldRender", "net.minecraft.client.renderer.entity.EntityRenderer", exception.message)
                    }

                } else
                    logTransformError("function shouldRender", "net.minecraft.client.renderer.entity.EntityRenderer", "Unable to find function to transform")

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
                        var successes = 0

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            var instruction = oldInstructions.get(i)

                            if (checkObfuscatedMethodInsn(instruction, Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/LivingEntity", "knockBack", "func_70653_a",
                                    "(Lnet/minecraft/entity/Entity;FDD)V")) {
                                // Offsets the x-position of knockback.
                                changeX: for (; i >= 0; i--) {
                                    instruction = oldInstructions.get(i)

                                    if (checkVarInsn(instruction, Opcodes.ALOAD, 7)
                                            && checkObfuscatedMethodInsn(oldInstructions.get(i + 1), Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosX", "func_226277_ct_", "()D")) {
                                        var newInstructions = new InsnList()

                                        // ...
                                        // ALOAD 7
                                        // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosX ()D
                                        oldInstructions.insert(oldInstructions.get(i + 1), new InsnNode(Opcodes.DADD))
                                        // ...

                                        newInstructions.add(new InsnNode(Opcodes.DUP))
                                        newInstructions.add(new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                            "getOffsets",
                                            "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                            false
                                        ))
                                        newInstructions.add(new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                            "getVectorX",
                                            "(Lnet/minecraft/util/math/Vec3d;)D",
                                            false
                                        ))
                                        newInstructions.add(new InsnNode(Opcodes.DUP2_X1))
                                        newInstructions.add(new InsnNode(Opcodes.POP2))

                                        // ...
                                        // ALOAD 7
                                        oldInstructions.insert(instruction, newInstructions)
                                        // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosX ()D
                                        // DADD
                                        // ...

                                        successes |= 2
                                        break changeX
                                    }
                                }

                                if (successes & 2 === 0) {
                                    logTransformError("function attackEntityFrom", "net.minecraft.entity.LivingEntity", "Unable to find primary injection point")

                                } else {
                                    // Offsets the z-position of knockback.
                                    changeZ: for (; i < oldInstructions.size(); i++) {
                                        instruction = oldInstructions.get(i)

                                        if (checkVarInsn(instruction, Opcodes.ALOAD, 7)
                                                && checkObfuscatedMethodInsn(oldInstructions.get(i + 1), Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/Entity", "getPosZ", "func_226281_cx_", "()D")) {
                                            var newInstructions = new InsnList()

                                            // ...
                                            // ALOAD 7
                                            // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosZ ()D
                                            oldInstructions.insert(oldInstructions.get(i + 1), new InsnNode(Opcodes.DADD))
                                            // ...

                                            newInstructions.add(new InsnNode(Opcodes.DUP))
                                            newInstructions.add(new MethodInsnNode(
                                                Opcodes.INVOKESTATIC,
                                                "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                                "getOffsets",
                                                "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                                false
                                            ))
                                            newInstructions.add(new MethodInsnNode(
                                                Opcodes.INVOKESTATIC,
                                                "com/epiphany/isawedthisplayerinhalf/helpers/BytecodeHelper",
                                                "getVectorZ",
                                                "(Lnet/minecraft/util/math/Vec3d;)D",
                                                false
                                            ))
                                            newInstructions.add(new InsnNode(Opcodes.DUP2_X1))
                                            newInstructions.add(new InsnNode(Opcodes.POP2))

                                            // ...
                                            // ALOAD 7
                                            oldInstructions.insert(instruction, newInstructions)
                                            // INVOKEVIRTUAL net/minecraft/entity/Entity.getPosZ ()D
                                            // DADD
                                            // ...

                                            successes |= 4
                                            break changeZ
                                        }
                                    }

                                    if (successes & 4 === 0)
                                        logTransformError("function attackEntityFrom", "net.minecraft.entity.LivingEntity", "Unable to find secondary injection point")
                                }

                                successes |= 1
                                break
                            }
                        }

                        if (successes & 1 === 0) {
                            logTransformError("function attackEntityFrom", "net.minecraft.entity.LivingEntity", "Unable to find injection area")

                        } else if (successes & 7 === 7)
                            logTransformSuccess("function attackEntityFrom", "net.minecraft.entity.LivingEntity")

                    } catch (exception) {
                        logTransformError("function attackEntityFrom", "net.minecraft.entity.LivingEntity", exception.message)
                    }

                } else
                    logTransformError("function attackEntityFrom", "net.minecraft.entity.LivingEntity", "Unable to function to transform")

                return classNode
            }
        },
    }
}