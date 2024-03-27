var Opcodes = Java.type("org.objectweb.asm.Opcodes")
var InsnList = Java.type("org.objectweb.asm.tree.InsnList")
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode")
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode")



/*
 * MIT License
 *
 * Copyright (c) 2021 ona-li-toki-e-jan-Epiphany-tawa-mi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/

/**
 * Custom event and event listeners injection.
 */



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
 * Checks if an instruction node has the given opcode.
 *
 * @param {object/InsnNode} instructionNode The instruction node to check.
 * @param {number} opCode The opcode the instruction should have.
 */
function checkInsn(instructionNode, opCode) {
    return instructionNode.getOpcode() === opCode
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
    print("[" + currentDate.getHours() + ":" + currentDate.getMinutes() + ":" + currentDate.getSeconds() + "] [ISawedThisPlayerInHalf/EventInjectorCore/" +
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

var ErrorMessages = {
    injectionPointNotFound: "Unable to find injection point",
    functionNotFound: "Unable to find function to transform"
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
         * Calls event listeners approximately when the client finishes loading onto a dedicated server.
         */
        "ClientPlayNetHandler": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.client.network.play.ClientPlayNetHandler"
            },

            "transformer": function(classNode) {
                var classPath = "net.minecraft.client.network.play.ClientPlayNetHandler"

                var handleJoinGame = findObfuscatedMethodWithSignature(classNode, "handleJoinGame", "func_147282_a", "(Lnet/minecraft/network/play/server/SJoinGamePacket;)V")
                var functionName = "function handleJoinGame"

                if (handleJoinGame !== null) {
                    try {
                        var oldInstructions = handleJoinGame.instructions
                        var success = false

                        for (var i = 0; i < oldInstructions.size(); i++) {
                            if (checkObfuscatedMethodInsn(oldInstructions.get(i), Opcodes.INVOKEVIRTUAL, "net/minecraft/client/MinecraftGame", "startGameSession", "func_216814_a", "()V")) {
                                // ...
                                // INVOKEVIRTUAL net/minecraft/client/MinecraftGame.startGameSession ()V
                                oldInstructions.insert(oldInstructions.get(i), new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "onPostHandleJoinGame",
                                    "()V",
                                    false
                                ))
                                // ...

                                success = true
                                logTransformSuccess(functionName, classPath)

                                break
                            }
                        }

                        if (!success)
                            logTransformError(functionName, classPath, ErrorMessages.injectionPointNotFound)

                    } catch (exception) {
                        logTransformError(functionName, classPath, exception.message)
                    }

                } else
                    logTransformError(functionName, classPath, ErrorMessages.functionNotFound)

                return classNode
            }
        },

        /**
         * Calls event listeners after an entity is assigned its entity id.
         * Calls event listeners when an entity is being unloaded.
         */
        "ClientWorld": {
            "target": {
                "type": "CLASS",
                "name": "net.minecraft.client.world.ClientWorld"
            },

            "transformer": function(classNode) {
                var classPath = "net.minecraft.client.world.ClientWorld"

                // Calls event listeners after an entity is assigned its entity id.
                var addEntityImpl = findObfuscatedMethodWithSignature(classNode, "addEntityImpl", "func_217424_b", "(ILnet/minecraft/entity/Entity;)V")
                var functionName = "function addEntityImpl"

                if (addEntityImpl !== null) {
                    try {
                        var oldInstructions = addEntityImpl.instructions
                        var success = false

                        for (var i = 0; i <= oldInstructions.size() - 2; i++) {
                            if (checkMethodInsn(oldInstructions.get(i), Opcodes.INVOKEINTERFACE, "it/unimi/dsi/fastutil/ints/Int2ObjectMap", "put", "(ILjava/lang/Object;)Ljava/lang/Object;")
                                    && checkInsn(oldInstructions.get(i+1), Opcodes.POP)) {
                                var callListeners = new InsnList()

                                callListeners.add(new VarInsnNode(Opcodes.ILOAD, 1)) // entityIdIn I
                                callListeners.add(new VarInsnNode(Opcodes.ALOAD, 2)) // entityToSpawn Lnet/minecraft/entity/Entity;
                                callListeners.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/epiphany/isawedthisplayerinhalf/Offsetter",
                                    "onPostEntityLoad",
                                    "(ILnet/minecraft/entity/Entity;)V",
                                    false
                                ))

                                // ...
                                // INVOKEINTERFACE it/unimi/dsi/fastutil/ints/Int2ObjectMap.put (ILjava/lang/Object;)Ljava/lang/Object;
                                // POP
                                oldInstructions.insert(oldInstructions.get(i+1), callListeners)
                                // ...

                                success = true
                                logTransformSuccess(functionName, classPath)

                                break
                            }
                        }

                        if (!success)
                            logTransformError(functionName, classPath, ErrorMessages.injectionPointNotFound)

                    } catch (exception) {
                        logTransformError(functionName, classPath, exception.message)
                    }

                } else
                    logTransformError(functionName, classPath, ErrorMessages.functionNotFound)

                // Calls event listeners when an entity is being unloaded.
                var removeEntity = findObfuscatedMethodWithSignature(classNode, "removeEntity", "func_217414_d", "(Lnet/minecraft/entity/Entity;)V")
                functionName = "function removeEntity"

                if (removeEntity !== null) {
                    try {
                        var callListeners = new InsnList()

                        callListeners.add(new VarInsnNode(Opcodes.ALOAD, 1)) // entityIn Lnet/minecraft/entity/Entity;
                        callListeners.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "com/epiphany/isawedthisplayerinhalf/Offsetter",
                            "onEntityUnload",
                            "(Lnet/minecraft/entity/Entity;)V",
                            false
                        ))

                        // METHOD START.
                        removeEntity.instructions.insert(callListeners)
                        // ...

                        logTransformSuccess(functionName, classPath)


                    } catch (exception) {
                        logTransformError(functionName, classPath, exception.message)
                    }

                } else
                    logTransformError(functionName, classPath, ErrorMessages.functionNotFound)

                return classNode
            }
        }
    }
}
