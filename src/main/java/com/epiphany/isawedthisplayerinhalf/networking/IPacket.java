package com.epiphany.isawedthisplayerinhalf.networking;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

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
 * Interface for enforcing some methods that are needed to register the packet so that it can be used.
 * Classes implementing this will need a constructor that accepts a PacketBuffer.
 */
public interface IPacket {
    /**
     * Converts the data of the packet into bytes and loads it into a packet buffer.
     *
     * @param packetBuffer The packet buffer to load data into.
     */
    void toBytes(PacketBuffer packetBuffer);

    /**
     * Handles the packet once it arrives from the other side of the network. (Server to client or client to server.)
     *
     * @param contextSupplier A supplier to the context behind the packet handle call.
     */
    void handle(Supplier<NetworkEvent.Context> contextSupplier);
}
