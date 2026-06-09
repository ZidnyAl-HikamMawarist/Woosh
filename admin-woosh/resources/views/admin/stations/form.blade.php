@extends('layouts.admin')

@section('title', isset($station) ? 'Edit Stasiun' : 'Tambah Stasiun')

@section('content')
    <div class="card" style="max-width: 600px; margin: 0 auto;">
        <div class="card-header">
            <h3 class="card-title">{{ isset($station) ? 'Edit Stasiun' : 'Tambah Stasiun Baru' }}</h3>
            <a href="{{ route('admin.stations') }}" class="btn btn-secondary">Kembali</a>
        </div>

        <form action="{{ isset($station) ? route('admin.stations.update', $station->id) : route('admin.stations.store') }}" method="POST">
            @csrf
            @if(isset($station))
                @method('PUT')
            @endif

            <div class="form-group">
                <label for="name">Nama Stasiun</label>
                <input type="text" name="name" id="name" class="form-control" value="{{ old('name', $station->name ?? '') }}" placeholder="Contoh: Stasiun Halim" required>
            </div>

            <div class="form-group">
                <label for="code">Kode Stasiun (3 Huruf)</label>
                <input type="text" name="code" id="code" class="form-control" value="{{ old('code', $station->code ?? '') }}" placeholder="HLM" required>
            </div>

            <div class="form-group">
                <label for="city">Kota</label>
                <input type="text" name="city" id="city" class="form-control" value="{{ old('city', $station->city ?? '') }}" placeholder="Jakarta Timur" required>
            </div>

            <div class="form-group">
                <label for="facilities">Fasilitas</label>
                <textarea name="facilities" id="facilities" class="form-control" rows="3">{{ old('facilities', $station->facilities ?? '') }}</textarea>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                <div class="form-group">
                    <label for="latitude">Latitude</label>
                    <input type="text" name="latitude" id="latitude" class="form-control" value="{{ old('latitude', $station->latitude ?? '') }}" placeholder="-6.2464">
                </div>
                <div class="form-group">
                    <label for="longitude">Longitude</label>
                    <input type="text" name="longitude" id="longitude" class="form-control" value="{{ old('longitude', $station->longitude ?? '') }}" placeholder="106.8906">
                </div>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 20px;">
                {{ isset($station) ? 'Simpan Perubahan' : 'Tambah Stasiun' }}
            </button>
        </form>
    </div>
@endsection
