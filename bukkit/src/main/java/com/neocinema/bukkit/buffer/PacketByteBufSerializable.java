package com.neocinema.bukkit.buffer;

public interface PacketByteBufSerializable<T extends PacketByteBufSerializable> {

    T fromBytes(PacketByteBufReimpl buf);

    void toBytes(PacketByteBufReimpl buf);

}
