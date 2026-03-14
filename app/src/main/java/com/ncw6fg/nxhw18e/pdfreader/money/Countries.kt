package com.ncw6fg.nxhw18e.pdfreader.money

import android.os.Parcel
import android.os.Parcelable

/**
 * create by colin
 * 2024/7/4
 */
data class Countries(
    val name: String,
    val code: String = "",
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()?:"",
        parcel.readString()?:""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(code)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Countries> {
        override fun createFromParcel(parcel: Parcel): Countries {
            return Countries(parcel)
        }

        override fun newArray(size: Int): Array<Countries?> {
            return arrayOfNulls(size)
        }
    }
}
